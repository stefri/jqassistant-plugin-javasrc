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

import scala.annotation.tailrec
import scala.collection.JavaConversions._

trait TreeWalker {

  def visitors: Iterable[TreeVisitor]

  @tailrec
  final def walk(nodes: IndexedSeq[EnhancedCommonTree]) {

    // Visit first node in list
    val node = nodes(0)
    visitors foreach { _.visit(node) }

    // Remove first element from node list and prepend all node children afterwards
    val leftNodes =
      if (node.getChildCount() > 0) node.getChildren().toIndexedSeq ++ nodes.tail
      else nodes.tail

    // Do next walk if there are still nodes left, return otherwise
    leftNodes.size match {
      case 0 => None
      case _ => walk(leftNodes)
    }
  }
}
