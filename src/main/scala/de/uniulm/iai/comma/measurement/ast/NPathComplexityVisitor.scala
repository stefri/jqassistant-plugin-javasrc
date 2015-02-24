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

import de.uniulm.iai.comma.lib.ast.TreeWrapper._
import de.uniulm.iai.comma.lib.ast.javasource.EnhancedCommonTree
import de.uniulm.iai.comma.lib.ast.javasource.JavaParser._
import de.uniulm.iai.comma.model.{Value, Change, Measure}

import scala.collection.JavaConversions._

object NPathComplexityVisitor extends TreeVisitorFactory {

  def measures(): Iterable[Measure] = Vector(Measure.NPATH)

  def createVisitor(entity: Change, artifact: Option[String]): NPathComplexityVisitor = {
    new NPathComplexityVisitor(entity, artifact)
  }
}

class NPathComplexityVisitor(entity: Change, artifact: Option[String] = None) extends TreeVisitor {
  val maxThreshold = 16000
  private var npath: BigInt = 0

  /** Visitor kicks in on method and constructor declarations. It ignores all other tokens. */
  override def visit(node: EnhancedCommonTree) = node.getType match {
    case VOID_METHOD_DECL
       | FUNCTION_METHOD_DECL
       | ANNOTATION_METHOD_DECL
       | CONSTRUCTOR_DECL
      => {
        npath = node.findChildNode(BLOCK_SCOPE) match {
          case Some(block) => complexityMultipleOf(block, 1)
          case None => 1
        }
      }
    case _ => // ignore
  }


  /** Returns the overall npath value for the visited construct (depends where the visitor is attached). */
  override def measuredValues(): Iterable[Value] = {
    if (npath > maxThreshold) {
      Vector(
        Value(artifact, Measure.NPATH_MAX_EXCEEDED, 1)
      )
    } else {
      Vector(
        Value(artifact, Measure.NPATH, npath.toLong)
      )
    }
  }


  /** NPath for given node based on multiply of all children npaths. */
  private def complexityMultipleOf(node: EnhancedCommonTree, npathStart: Long = 1): Long = {
    if (node.getChildCount > 0) {
      node.getChildren.foldLeft(npathStart) { (npath, c) =>
        val res = visitNode(c)
        (if(npath == 0) 1 else npath) * (if (res == 0) 1 else res)
      }
    } else npathStart
  }


  /** NPath for given node based on sum of all children npaths. */
  private def complexitySumOf(node: EnhancedCommonTree, npathStart: Long = 0): Long = {
    if (node.getChildCount > 0) {
      node.getChildren.foldLeft(npathStart) { (npath, c) =>
        npath + visitNode(c)
      }
    } else npathStart
  }


  /**
   * This method delegates child visits to the appropriate token-handlers to calculate
   * the statement specific values (if-else, for, switch ...). All method-call tokens
   * return 1 immediately. If a logical and or a logical or is visited, it is counted
   * as one and summed up with all of its childs.
   *
   * All other tokens are pushed to the complexitySumOf-method to handle
   * child-statements.
   *
   * @param node
   * @return npath of visited node
   */
  private def visitNode(node: EnhancedCommonTree): Long = node.getType match {
    case IF       => visitIfStatement(node)
    case FOR      => visitForStatement(node)
    case FOR_EACH => visitForEachStatement(node)
    case WHILE    => visitWhileStatement(node)
    case DO       => visitDoStatement(node)
    case QUESTION => visitConditionalExpression(node)
    case TRY      => visitTryStatement(node)
    case SWITCH   => visitSwitchStatement(node)
    case RETURN   => visitReturnStatement(node)

    case METHOD_CALL
         | CLASS_CONSTRUCTOR_CALL
         | THIS_CONSTRUCTOR_CALL
         | SUPER_CONSTRUCTOR_CALL
         | BREAK
         => 1

    case LOGICAL_AND
         | LOGICAL_OR
         => complexitySumOf(node, 1)

    case BLOCK_SCOPE => complexityMultipleOf(node, 0)

    case _ => complexitySumOf(node)
  }


  /**
   * NPath of if-statements and if-else-statements is implemented as outlined in the original
   * paper. Both cases are handled by this method. First the if-range is calculated, then the
   * else-range (or 1 if there is no else) and then the if-expression is added.
   *
   * @param node
   * @return npath of if and if-else constructs
   */
  private def visitIfStatement(node: EnhancedCommonTree): Long = {

    // Calculate the if-range. First Child is the expression, second child the range to consider.
    val ifRange = visitNode(node.childAt(1).get)

    // The else path is also a child, so the if-npath includes the else-npath if an else
    // is defined -- if not, 1 is added.
    val elseRange = node.findChildNode(ELSE) match {
      case Some(elseNode) => visitNode(elseNode.childAt(0).get)
      case None => 1
    }

    // Third, calculate the if-expression complexity.
    val expr = sumExpressionComplexity(node.findChildNode(PARENTESIZED_EXPR).get)

    ifRange + elseRange + expr
  }


  /**
   * NPath of while-loop is implemented as outlined in the original paper.
   *
   * NP(while) = NP(while-range) + NP(expr) + 1
   *
   * @param node
   * @return npath of while-loop
   */
  private def visitWhileStatement(node: EnhancedCommonTree): Long = {
    // Calculate the while-range. First child is the expression, second child the range to consider.
    val whileRange = visitNode(node.childAt(1).get)

    // Second, calculate the while-expression complexity.
    val expr = sumExpressionComplexity(node.findChildNode(PARENTESIZED_EXPR).get)

    whileRange + expr + 1
  }


  /**
   * NPath of do-while-loops is implemented as outlined in the original paper.
   *
   * NP(do) = NP(do-range) + NP(expr) + 1
   *
   * @param node
   * @return npath of do-while-loop
   */
  private def visitDoStatement(node: EnhancedCommonTree): Long = {
    // Calculate the do-range. First child is the expression, second child the range to consider.
    val doRange = visitNode(node.childAt(0).get)

    // Second, calculate the while-expression complexity.
    val expr = sumExpressionComplexity(node.findChildNode(PARENTESIZED_EXPR).get)

    doRange + expr + 1
  }


  /**
   * NPath of for-loops is implemented as outlined in the original paper.
   *
   * NP(for) = NP(for-range) + NP(init-expr) + NP(condition-expr) + NP(update-expr) + 1
   *
   * @param node
   * @return npath of for-loop
   */
  private def visitForStatement(node: EnhancedCommonTree): Long = {
    // Calculate for-range
    val forRange = visitNode(node.childAt(3).get)

    // Calculate for-init-expression
    val forInit = node.findChildNode(FOR_INIT).fold(0L)(sumExpressionComplexity(_))

    // Calculate for-condition-expression
    val forCond = node.findChildNode(FOR_CONDITION).fold(0L)(sumExpressionComplexity(_))

    // Calculate for-update-expression
    val forUpdate = node.findChildNode(FOR_UPDATE).fold(0L)(sumExpressionComplexity(_))

    forRange + forInit + forCond + forUpdate + 1
  }


  /**
   * The for-each statement is no c-structure, so the npath calculation is not
   * described in the original paper. Basically, its a for-loop and could be written as
   * <code>for (int i = 0; i < iterator.length(); i++) { ... }</code>. Given that, it is
   * obvious, that only the iterator-creating part of the for-loop might add additional
   * complexity. There is a hidden method-call to determine the iterator length, but since
   * it's hidden, it does not add additional complexity and should not be counted.
   *
   * NP(foreach) = NP(foreach-range) + NP(iterator-expr) + 1
   *
   * @param node
   * @return npath of foreach
   */
  private def visitForEachStatement(node: EnhancedCommonTree): Long = {
    // Calculate for-each-range
    val forEachRange = visitNode(node.childAt(4).get)

    // Calculate iterator-creating expression
    val expr = complexitySumOf(node.childAt(3).get)

    forEachRange + expr + 1
  }


  /**
   * The npath for a return should be one if there is no following expression or
   * the npath of the following expression (at least 1).
   *
   * @param node
   * @return npath of a return-statement
   */
  private def visitReturnStatement(node: EnhancedCommonTree): Long = {
    node.findChildNode(EXPR).fold(1L)(complexityMultipleOf(_))
  }


  /**
   * NPath for switch statement is calculated straightforward as outlined in the
   * original paper.
   *
   * NP(switch) = NP(expr) + NP(default-range) + SUM(NP(case-ranges))
   *
   * @param node
   * @return npath of switch-statement
   */
  private def visitSwitchStatement(node: EnhancedCommonTree): Long = {
    val expr = sumExpressionComplexity(node.findChildNode(PARENTESIZED_EXPR).get)
    val switchBlock = node.findChildNode(SWITCH_BLOCK_LABEL_LIST).get
    val defaultRange = switchBlock.findChildNode(DEFAULT).fold(0L)(complexityMultipleOf(_))
    val caseRanges = switchBlock.getChildren.foldLeft(1L)((cur, c) => c.getType match {
      case CASE => cur + complexityMultipleOf(c)
      case _ => cur
    })

    expr + defaultRange + caseRanges
  }


  /**
   * The original paper did not address try-catch-finally blocks. Based on the principles
   * outlined for other blocks, as well as the PMD and Checkstyle NPath implementations,
   * this method will calculate the complexity of the try-range, the finally-range and
   * similar to a switch for all catch-ranges and sum them up.
   *
   * So in terms of the original paper this would be:
   * NP(try-catch-finally) = NP(try-range) + NP(finally-range) + SUM(NP(catch-ranges)
   *
   * @param node
   * @return npath of try-catch-finally statement
   */
  private def visitTryStatement(node: EnhancedCommonTree): Long = {
    val tryRange = visitNode(node.childAt(0).get)

    val catchRanges = node.findChildNode(CATCH_CLAUSE_LIST).fold(0L){ c =>
      c.getChildren.foldLeft(0L) { (cur, catchNode) =>
        cur + visitNode(node.childAt(0).get)
      }
    }
    val finallyRange = node.findChildNode(FINALLY).fold(0L)(complexityMultipleOf(_))

    tryRange + finallyRange + catchRanges
  }


  /**
   * NPath for a conditional expression (tenary-operator) is the sum of all three
   * enclosed expressions plus 2. Hence, it is possible to delegate computation to
   * the complexitySumOf-Method.
   *
   * NP(?) = NP(epxr1) + NP(expr2) + NP(expr3) + 2
   *
   * @param node
   * @return npath of conditional expression
   */
  private def visitConditionalExpression(node: EnhancedCommonTree): Long = {
    complexitySumOf(node) + 2
  }


  /**
   * Calculate the boolean complexity of the given expression. In fact this is just delegating
   * to the complexitySumOf-Method. Boolean expressions might include function-calls, so
   * we have to make sure those are evaluated as well.
   *
   * @param node
   * @return npath of expression
   */
  private def sumExpressionComplexity(node: EnhancedCommonTree): Long = {
    complexitySumOf(node)
  }
}
