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
import de.uniulm.iai.comma.model.{Value, Change, Measure}

object CyclomaticComplexityVisitor extends TreeVisitorFactory {

  override def measures() = Vector(Measure.CCN)

  override def createVisitor(entity: Change, artifact: Option[String]): CyclomaticComplexityVisitor = {
    new CyclomaticComplexityVisitor(entity, artifact)
  }

}

class CyclomaticComplexityVisitor(entity: Change, artifact: Option[String]) extends TreeVisitor {

  private var counter: Long = 1
  def ccn = counter

  def visit(node: EnhancedCommonTree) = node.getType match {

    // @TODO Should I include RETURN if it is not the last statement of a method?
    case IF
      | FOR
      | FOR_EACH
      | WHILE
      | DO
      | CASE
      | CATCH
      | LOGICAL_AND
      | LOGICAL_OR
      | QUESTION => {
      counter += 1
    }

    case _ => // silently ignore unmatched tokens
  }

  override def measuredValues() = Vector(Value(artifact, Measure.CCN, ccn))

}
