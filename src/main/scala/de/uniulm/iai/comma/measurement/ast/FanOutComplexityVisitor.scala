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
import de.uniulm.iai.comma.model.{Measure, Value, Change}

import scala.annotation.tailrec
import scala.collection.JavaConversions._

object FanOutComplexityVisitor extends TreeVisitorFactory {

  override def measures() = Vector()

  def apply(entity: Change, artifact: Option[String] = None) = {
    val ignoredClassesBuilder = Set.newBuilder[String]
    ignoredClassesBuilder += "boolean"
    ignoredClassesBuilder += "byte"
    ignoredClassesBuilder += "char"
    ignoredClassesBuilder += "double"
    ignoredClassesBuilder += "float"
    ignoredClassesBuilder += "int"
    ignoredClassesBuilder += "long"
    ignoredClassesBuilder += "short"
    ignoredClassesBuilder += "void"
    ignoredClassesBuilder += "Boolean"
    ignoredClassesBuilder += "Byte"
    ignoredClassesBuilder += "Character"
    ignoredClassesBuilder += "Double"
    ignoredClassesBuilder += "Float"
    ignoredClassesBuilder += "Integer"
    ignoredClassesBuilder += "Long"
    ignoredClassesBuilder += "Object"
    ignoredClassesBuilder += "Short"
    ignoredClassesBuilder += "String"
    ignoredClassesBuilder += "Void"
    ignoredClassesBuilder += "Exception"
    ignoredClassesBuilder += "RuntimeException"
    ignoredClassesBuilder += "Throwable"
    new FanOutComplexityVisitor(entity, artifact, ignoredClassesBuilder.result())
  }

  def createVisitor(entity: Change, artifact: Option[String]): FanOutComplexityVisitor = {
    apply(entity, artifact)
  }
}

class FanOutComplexityVisitor(entity: Change, artifact: Option[String], ignoredClasses: Set[String])
    extends TreeVisitor {

  val foundClasses = Set.newBuilder[String]

  def visit(node: EnhancedCommonTree) = node.getType match {

    case TYPE | THROWS_CLAUSE =>  {
      evaluateChilds(node, stringifyTypes)
    }

    case STATIC_ARRAY_CREATOR | CLASS_CONSTRUCTOR_CALL | ANONYMOUS_CLASS_CONSTRUCTOR_CALL => {
      evaluateChilds(node, stringifyCall)
    }

    case _ => // Ignore
  }

  override def measuredValues(): Iterable[Value] = {
    Vector(new Value(artifact, Measure.FANOUT, foundClasses.result().size))
  }


  private def evaluateChilds(node: EnhancedCommonTree, evaluator: (IndexedSeq[EnhancedCommonTree], String) => String):
      Unit = {

    val classes = if (node.getChildren != null) evaluator(node.getChildren.toIndexedSeq, "") else ""
    classes.split(',').foreach( c => if (isSignificant(c)) foundClasses += c)
  }

  private def isSignificant(className: String): Boolean = {
    (!className.isEmpty) && !ignoredClasses.contains(className) && !className.startsWith("java.lang")
  }

  @tailrec
  private def stringifyCall(nodes: IndexedSeq[EnhancedCommonTree], text: String = ""): String = {
    var newText = text
    val node = nodes.get(0)
    val leftNodes =
      if (node.getChildren != null) {
        node.getType match {
          case ARGUMENT_LIST => nodes.tail
          case CLASS_TOP_LEVEL_SCOPE => nodes.tail
          case GENERIC_TYPE_ARG_LIST => nodes.tail
          case _ => {
            newText = newText + node.getText
            node.getChildren.toIndexedSeq ++ nodes.tail
          }
        }
      } else {
        newText = newText + node.getText
        nodes.tail
      }

    if (leftNodes.isEmpty) newText
    else stringifyCall(leftNodes, newText)
  }


  @tailrec
  private def stringifyTypes(nodes: IndexedSeq[EnhancedCommonTree], text: String = ""): String = {
    var newText = text

    val node = nodes.get(0)
    val leftNodes =
      if (node.getChildren != null) {
        node.getChildren.toIndexedSeq ++ nodes.tail
      } else {
        newText =
          node.getType match {
            case LOCAL_MODIFIER_LIST | SEMI | ARRAY_DECLARATOR | QUESTION => text
            case SUPER | EXTENDS | LESS_THAN | SHIFT_LEFT => text + ","
            case GREATER_THAN | SHIFT_RIGHT | BIT_SHIFT_RIGHT => text
            case IDENT => {
              text + {
                if (!text.isEmpty && text.last != '.' && text.last != ',') "."
                else ""
              } + node.getText
            }
            case _ => text + node.getText
          }
        nodes.tail
      }

    if (leftNodes.isEmpty) newText.replaceAll(",+", ",").stripSuffix(",")
    else stringifyTypes(leftNodes, newText)
  }
}
