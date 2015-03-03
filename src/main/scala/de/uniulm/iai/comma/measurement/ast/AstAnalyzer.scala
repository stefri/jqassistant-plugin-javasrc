/*
 * comma, A Code Measurement and Analysis Tool
 * Copyright (C) 2010-2015 Steffen Kram
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.uniulm.iai.comma.measurement.ast

import java.io.Reader

import de.uniulm.iai.comma.lib.ast.javasource._
import de.uniulm.iai.comma.model.Change
import org.antlr.runtime.{ANTLRReaderStream, CommonTokenStream, Token}
import org.apache.commons.logging.LogFactory

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.{SortedMap, mutable}
import scala.util.{Failure, Success, Try}

trait AstAnalyzer extends TreeWalker {
  private val logger = LogFactory.getLog(this.getClass)

  private val visitorReg = Vector.newBuilder[TreeVisitor]
  override def visitors = visitorReg.result()

  def addVisitor(visitor: TreeVisitor) = visitorReg += visitor

  def runWith(change: Change, src: Reader): Unit = {
    val input = new ANTLRReaderStream(src)

    // Setup lexer to preserve all comments
    val lexer = new JavaLexer(input)
    lexer.mPreserveBlockComments = true
    lexer.mPreserveJavaDocComments = true
    lexer.mPreserveLineComments = true

    // Create custom comment collector
    val commentTokens = List[java.lang.Integer](
      JavaLexer.BLOCK_COMMENT,
      JavaLexer.LINE_COMMENT,
      JavaLexer.JAVADOC_COMMENT)
    val tokenSource = new CollectorTokenSource(lexer, commentTokens)


    // Pass comment-enhanced token stream to generated parser
    val tokens = new CommonTokenStream(tokenSource)
    val parser = new JavaParser(tokens)
    parser.enableErrorMessageCollection(true)
    parser.setTreeAdaptor(new EnhancedCommonTreeAdapter)

    // Parse java source
    val javaSource = parser.javaSource
    if (!parser.hasErrors) {
      val tree = Vector(javaSource.getTree.asInstanceOf[EnhancedCommonTree])

      val leftTokens = addHiddenTokens(
          None,
          orderedNodeList(tree),
          SortedMap(tokenSource.getCollectedTokens.map(t => (t.getTokenIndex, t)): _*))
      if (!leftTokens.isEmpty) {
        logger.warn(s"Unable to match ${leftTokens.size} tokens: \n${leftTokens}")
      }

      walk(tree)
    } else {
      parser.getMessages.foreach(println)
    }
  }

  private def orderedNodeList(nodes: IndexedSeq[EnhancedCommonTree]): SortedMap[Int, EnhancedCommonTree] = {

    @tailrec
    def buildMap(nodes: IndexedSeq[EnhancedCommonTree],
                         builder: mutable.Builder[(Int, EnhancedCommonTree), SortedMap[Int, EnhancedCommonTree]]):
    SortedMap[Int, EnhancedCommonTree] = {

      val node = nodes(0)
      if (node.getToken.getTokenIndex >= 0) builder += ((node.getToken.getTokenIndex, node))

      val leftNodes = if (node.getChildCount > 0) node.getChildren.toIndexedSeq ++ nodes.tail else nodes.tail
      if (leftNodes.isEmpty) builder.result() else buildMap(leftNodes, builder)
    }

    buildMap(nodes, SortedMap.newBuilder[Int, EnhancedCommonTree])
  }

  @tailrec
  private def addHiddenTokens(
      previousNode: Option[EnhancedCommonTree],
      nodes: SortedMap[Int, EnhancedCommonTree],
      tokens: SortedMap[Int, Token]):
      IndexedSeq[Token] = {

    // Fetch all tokens with a token index less than current node index
    val node = nodes.head
    val currentTokens = tokens.takeWhile(t => t._1 < node._1).map(_._2)

    // Add all current tokens with same line number as previous node as following tokens to the previous node
    val previousTokens =
      if (previousNode.isDefined) {
        val res = currentTokens.takeWhile(t => t.getLine == previousNode.get.getLastLine)
        previousNode.get.addFollowing(res)
        res
      } else {
        Iterable.empty[Token]
      }


    // Add all other tokens as preceding tokens to the current node
    val precedingTokens = currentTokens.drop(previousTokens.size)
    node._2.addPreceding(precedingTokens)

    val leftTokens = tokens.drop(currentTokens.size)
    val leftNodes = nodes.tail
    if (leftTokens.isEmpty || leftNodes.isEmpty) {

      // There might be remaining comment-tokens at the end of a file. Add them as following to the last node, if their
      // token-index is greater than the last nodes token index.
      val remainingTokens = leftTokens.partition(t => t._1 > node._1)
      node._2.addFollowing(remainingTokens._1.map(_._2))

      // Return any tokes that did not match so far ... should always be empty!
      remainingTokens._2.map(_._2).toVector

    } else {
      addHiddenTokens(Some(node._2), leftNodes, leftTokens)
    }
  }
}
