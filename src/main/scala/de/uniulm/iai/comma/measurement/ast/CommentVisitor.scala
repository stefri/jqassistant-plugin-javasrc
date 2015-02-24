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

import de.uniulm.iai.comma.lib.ast.javasource.{EnhancedCommonTree, JavaLexer}
import de.uniulm.iai.comma.model.{Value, Change, Measure}

import scala.collection.JavaConversions._

object CommentVisitor extends TreeVisitorFactory {

  override def measures() = Vector(
    Measure.LINE_COMMENT_COUNT,
    Measure.LINE_COMMENT_LENGTH,
    Measure.BLOCK_COMMENT_COUNT,
    Measure.BLOCK_COMMENT_LINES,
    Measure.BLOCK_COMMENT_LENGTH,
    Measure.JAVADOC_COUNT,
    Measure.JAVADOC_LINES,
    Measure.BLOCK_COMMENT_LENGTH
  )

  override def createVisitor(entity: Change, artifact: Option[String]): CommentVisitor = {
    new CommentVisitor(entity, artifact)
  }

}

class CommentVisitor(entity: Change, artifact: Option[String]) extends TreeVisitor {

  /** Store number of source level line comments. */
  private var _lineCommentCount = 0
  def lineCommentCount = _lineCommentCount

  /** Store total length of source level line comments. */
  private var _lineCommentLength = 0l
  def lineCommentLength = _lineCommentLength

  /** Store total number of block comments. */
  private var _blockCommentCount = 0
  def blockCommentCount = _blockCommentCount

  /** Store total number of source level block comment lines. */
  private var _blockCommentLines = 0
  def blockCommentLines = _blockCommentLines

  /** Store total length of source level block comments. */
  private var _blockCommentLength = 0l
  def blockCommentLength = _blockCommentLength

  /** Store total number of javadoc comments. */
  private var _javadocCommentCount = 0
  def javadocCommentCount = _javadocCommentCount

  /** Store total number of source level javadoc comment lines. */
  private var _javadocCommentLines = 0
  def javadocCommentLines = _javadocCommentLines

  /** Store total length of source level javadoc comments. */
  private var _javadocCommentLength = 0l
  def javadocCommentLength = _javadocCommentLength


  def visit(node: EnhancedCommonTree) = {
    (node.getPrecedingComments ++ node.getFollowingComments).foreach { t =>
      t.getType match {
        case JavaLexer.LINE_COMMENT => {
          _lineCommentCount += 1
          _lineCommentLength += t.getText.length
        }
        case JavaLexer.BLOCK_COMMENT => {
          _blockCommentCount += 1
          _blockCommentLines += t.getText.linesIterator.size
          _blockCommentLength += t.getText.length
        }
        case JavaLexer.JAVADOC_COMMENT => {
          _javadocCommentCount += 1
          _javadocCommentLines += t.getText.linesIterator.size
          _javadocCommentLength += t.getText.length
        }
        case _ => // ignore, ... should be never reached anyway.
      }
    }
  }

  /**
   * Comment values are only counted if the analysed structure is the topmost structure.
   * It does not make sense to aggregate comment counts across structure boundaries. If
   * thats what is needed, than a new visitor should fulfill that task.
   *
   * @param node
   * @param topmostStructure
   */
  override def visit(node: EnhancedCommonTree, topmostStructure: Boolean) {
    if (topmostStructure) visit(node)
  }

  override def measuredValues(): Iterable[Value] = {
    val builder = Vector.newBuilder[Value]

    if (lineCommentCount > 0) {
      builder += Value(artifact, Measure.LINE_COMMENT_COUNT, lineCommentCount)
      builder += Value(artifact, Measure.LINE_COMMENT_LENGTH, lineCommentLength)
    }

    if (blockCommentCount > 0) {
      builder += Value(artifact, Measure.BLOCK_COMMENT_COUNT, blockCommentCount)
      builder += Value(artifact, Measure.BLOCK_COMMENT_LINES, blockCommentLines)
      builder += Value(artifact, Measure.BLOCK_COMMENT_LENGTH, blockCommentLength)
    }

    if (javadocCommentCount > 0) {
      builder += Value(artifact, Measure.JAVADOC_COUNT, javadocCommentCount)
      builder += Value(artifact, Measure.JAVADOC_LINES, javadocCommentLines)
      builder += Value(artifact, Measure.JAVADOC_LENGTH, javadocCommentLength)
    }

    builder.result()
  }
}
