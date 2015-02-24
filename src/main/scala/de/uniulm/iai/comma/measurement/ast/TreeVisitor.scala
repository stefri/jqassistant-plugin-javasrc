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
import de.uniulm.iai.comma.model.Value

/** This is the main trait to implement for each custom visitor.
  *
  * Basically, it defines two methods, the `visit` method is called for each
  * tree node, so you must handle each node in your implementation. This is
  * best done with pattern matching. Second, you have to implement the
  * `measuredValues` method which is called at the end of the tree walk to
  * collect the gathered values.
  */
trait TreeVisitor {

  def visit(node: EnhancedCommonTree): Unit

  /**
   * This is the visit method called for sub-visitors of a structure visitor. The
   * second parameter indicates if the current visitor is part of the topmost structure
   * of the entire structure stack.
   *
   * Some visitors (e.g. the comment-visitor) need this information to produce correct results, namely they
   * only count values if the structure is the topmost one.
   *
   * @param node
   *    Current node to visit
   * @param topmostStructure
   *    Indication if this is the topmost structure on the node stack
   */
  def visit(node: EnhancedCommonTree, topmostStructure: Boolean): Unit = visit(node)

  def measuredValues(): Iterable[Value] = {
    Iterable.empty[Value]
  }

}
