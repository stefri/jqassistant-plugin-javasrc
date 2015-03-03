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

import de.uniulm.iai.comma.lib.ast.javasource.EnhancedCommonTree
import de.uniulm.iai.comma.lib.ast.javasource.JavaParser._
import de.uniulm.iai.comma.model.Visibility

import scala.annotation.tailrec
import scala.collection.JavaConversions._

/**
 * @author Steffen Kram
 */
trait VisitorHelper {

  @tailrec
  final def getFunctionIdentifier(nodes: IndexedSeq[EnhancedCommonTree]): String = {
    if (nodes.isEmpty) return "()"

    nodes.get(0).getType match {
      case IDENT => return nodes.get(0).getText
      case _ => getFunctionIdentifier(nodes.tail)
    }
  }

  @tailrec
  final def addToSignature(nodes: IndexedSeq[EnhancedCommonTree], signature: String): String = {

    // Special abort case for annotation, since they do not have a formal parameter list!
    if (nodes.isEmpty) return signature + "()"

    // Find signature
    nodes.get(0).getType match {
      case IDENT =>
        val sig = signature + nodes.get(0).getText
        addToSignature(nodes.tail, sig)
      case FORMAL_PARAM_LIST =>
        val types =
          if (nodes.get(0).getChildren != null)
            addToParamList(nodes.get(0).getChildren.toIndexedSeq, "")
          else ""
        signature + "(" + types + ")"
      case _ => addToSignature(nodes.tail, signature)
    }
  }

  @tailrec
  final def addToParamList(params: IndexedSeq[EnhancedCommonTree], paramList: String): String = {
    params.get(0).getType match {
      case FORMAL_PARAM_VARARG_DECL =>
        val reversed = stringifyNodes(params.get(0).getChildren.toIndexedSeq, "").split(' ').reverse
        val param = reversed.updated(1, reversed(1) + "...").reverse.foldLeft("")((cur, elem) => cur + " " + elem)
        val p = paramList + param
        if (params.length == 1) p.trim
        else addToParamList(params.tail, p.trim + ", ")
      case FORMAL_PARAM_STD_DECL =>
        val p = paramList + stringifyNodes(params.get(0).getChildren.toIndexedSeq, "")
        if (params.length == 1) p.trim
        else addToParamList(params.tail, p.trim + ", ")
      case _ =>
        addToParamList(params.tail, paramList.trim)
    }
  }

  @tailrec
  final def stringifyNodes(nodes: IndexedSeq[EnhancedCommonTree], text: String): String = {
    var newText = text

    val node = nodes.get(0)
    val leftNodes =
      if (node.getChildren != null) {
        node.getChildren.toIndexedSeq ++ nodes.tail
      } else {
        newText =
          node.getType match {
            case LOCAL_MODIFIER_LIST | SEMI => text
            case SUPER | EXTENDS => text + " " + node.getText
            case IDENT => {
              text + {
                if (!text.isEmpty && text.last != '.' && text.last != '<') " "
                else ""
              } + node.getText
            }
            case _ => text + node.getText
          }
        nodes.tail
      }

    if (leftNodes.size == 0) newText
    else stringifyNodes(leftNodes, newText)
  }

  /** Use this method to detect the identifier of a certain node among its children. */
  def findIdentifier(node: EnhancedCommonTree): Option[EnhancedCommonTree] = {
    node.getChildren find {
      _.getType == IDENT
    }
  }

  /**
   * Detect the visibility of a structural node. A structural node might be a
   * class, interface, enum or annotation or a method call.
   *
   * This method does look for a modifier list, if there is none the structure is package-protected
   * - no keyword is defined at all. If there is a modifier list, it still might contain no children
   * which results in package-protected visibility, too. Otherwise we look for the actual visibility
   * modifier and return an appropriate visibility or if none of the standard visibility modifiers
   * are found we end up with package-protected visibility, again.
   */
  def detectVisibility(node: EnhancedCommonTree): Visibility = {
    node.getChildren find {
      _.getType == MODIFIER_LIST
    } match {
      case Some(list) => {

        // In case there are no modifiers at all
        if (list.getChildren == null) return Visibility.DEFAULT

        // Otherwise pick a visibility modifier or return package visibility if there is none specified
        list.getChildren find {
          _.getType match {
            case PUBLIC | PROTECTED | PRIVATE => true
            case _ => false
          }
        } match {
          case Some(n) => n.getType match {
            case PUBLIC => Visibility.PUBLIC
            case PROTECTED => Visibility.PROTECTED
            case PRIVATE => Visibility.PRIVATE
            case _ => Visibility.DEFAULT
          }
          case None => Visibility.DEFAULT
        }
      }
      case None => Visibility.DEFAULT
    }
  }

  /**
   * Detect the mutability of a structural node.
   *
   * @param node
   * @return True if it is a final element.
   */
  def detectFinal(node: EnhancedCommonTree): Boolean = detectModifier(node, FINAL)

  /**
   * Determine if the sturctural node is a static artifact.
   *
   * @param node
   * @return True if it is a static element.
   */
  def detectStatic(node: EnhancedCommonTree): Boolean = detectModifier(node, STATIC)

  /**
   * Determine if the structural artifact is abstract.
   *
   * @param node
   * @return True if it is an abstract element.
   */
  def detectAbstract(node: EnhancedCommonTree): Boolean = detectModifier(node, ABSTRACT)

  def detectVolatile(node: EnhancedCommonTree): Boolean = detectModifier(node, VOLATILE)

  def detectTransient(node: EnhancedCommonTree): Boolean = detectModifier(node, TRANSIENT)

  def detectModifier(node: EnhancedCommonTree, modifier: Int): Boolean = {
    (for {
      list <- node.getChildren.find(_.getType == MODIFIER_LIST)
      modNode <- {
        if (list.getChildren == null) None
        else list.getChildren.find(_.getType == modifier).headOption
      }
    } yield modNode).isDefined
  }

  /**
   * Determine the type of a field or parameter.
   *
   * @param node
   * @return Type of field or node
   */
  def detectType(node: EnhancedCommonTree): Option[EnhancedCommonTree] = {
    for {
      list <- node.getChildren.find(_.getType == TYPE)
      typeNode <- list.getChildren.headOption
    } yield typeNode
  }

  def detectVariableDeclarators(node: EnhancedCommonTree): Iterable[EnhancedCommonTree] = {
    node.getChildren.find(_.getType == VAR_DECLARATOR_LIST) match {
      case Some(list) =>
        if (list.getChildren == null) Seq.empty[EnhancedCommonTree]
        else list.getChildren.filter(_.getType == VAR_DECLARATOR)
      case None => Seq.empty[EnhancedCommonTree]
    }
  }
}
