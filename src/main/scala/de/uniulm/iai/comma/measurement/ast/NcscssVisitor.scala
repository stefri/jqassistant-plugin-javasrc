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

import com.buschmais.jqassistant.core.store.api.model.Descriptor
import de.uniulm.iai.comma.lib.ast.javasource.EnhancedCommonTree
import de.uniulm.iai.comma.lib.ast.javasource.JavaParser._
import de.uniulm.iai.comma.model.{Value, Measure}
import de.uniulm.iai.jqassistant.javasrc.plugin.model.measure.NcscssDescriptor

object NcscssVisitor extends TreeVisitorFactory {

  override def measures() = Vector(Measure.NCSCSS)

  override def createVisitor(descriptor: Descriptor, artifact: Option[String]): NcscssVisitor = {
    new NcscssVisitor(descriptor.asInstanceOf[NcscssDescriptor], artifact)
  }

}


class NcscssVisitor(descriptor: NcscssDescriptor, artifact: Option[String]) extends TreeVisitor {

  var debug = false
  private var counter: Long = 0
  def ncss = counter

  def visit(node: EnhancedCommonTree) = node.getType match {

    // NCSCSS increasing tokens
    case PACKAGE // Package
      | IMPORT // Import
      | AT // Annotation
      | CLASS_DECLARATION // Class
      | ENUM // Enum
      | INTERFACE // Interface

      | ENUM_CONSTANT // Enum constants declarations
      | CLASS_STATIC_INITIALIZER // Static initialization block
      | CONSTRUCTOR_DECL // Constructor declaration
      | VOID_METHOD_DECL // Method declaration -> void
      | FUNCTION_METHOD_DECL // Method declaration -> some value
      | ANNOTATION_METHOD_DECL // Method declaration inside an annotation

      | VAR_DECLARATOR // Variable declaration within a variable declaration list
      | VAR_DECLARATION // Variable declaration

      | IF // if
      | ELSE // else
      | WHILE // while
      | DO // do .. while
      | FOR // for
      | FOR_EACH // foreach
      | SWITCH // switch
      | CASE // case
      | DEFAULT // default
      | BREAK // break
      | CONTINUE // continue
      | RETURN // return
      | THROW // throw
      | SYNCHRONIZED_BLOCK // synchronized
      | TRY // try
      | CATCH // catch
      | FINALLY // finally
      | ASSERT // assert

      | SUPER_CONSTRUCTOR_CALL // super constructor call
      | THIS_CONSTRUCTOR_CALL // this constructor call
      | METHOD_CALL // method call
      | ASSIGN // assignment "=" which is not part of a variable declaration
      | AND_ASSIGN // assignment "&="
      | BIT_SHIFT_RIGHT_ASSIGN // assignment ">>>="
      | DIV_ASSIGN // assignment "/="
      | MINUS_ASSIGN // assignment "-="
      | MOD_ASSIGN // assignment "%="
      | OR_ASSIGN // assignment "|="
      | PLUS_ASSIGN // assignment "+="
      | SHIFT_LEFT_ASSIGN // assignment "<<="
      | SHIFT_RIGHT_ASSIGN // assignment ">>="
      | STAR_ASSIGN // assignment "*="
      | XOR_ASSIGN // assignment "^="
      | POST_DEC // "post --"
      | POST_INC // "post ++"
      | PRE_DEC // "pre --"
      | PRE_INC // "pre ++"
      =>
      counter += 1
      if (debug) println(node + "           <====  counter + => " + counter)

    // NCSCSS double-increasing tokens
    case QUESTION =>
      counter += 2
      if (debug) println(node + "           <====  counter +2 => " + counter)

    // NCSCSS decreasing tokens
    case VAR_DECLARATOR_LIST =>
      if (debug) println(node + "           <====  counter - => " + counter)
      counter -= 1

    // All other tokens are ignored
    case _ => if (debug) println(node)
  }

  override def measuredValues() = {
    descriptor.setNcscss(counter)
    Vector(Value(artifact, Measure.NCSCSS, counter))
  }
}
