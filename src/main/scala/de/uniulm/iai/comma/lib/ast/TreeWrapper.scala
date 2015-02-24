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

package de.uniulm.iai.comma.lib.ast

import collection.JavaConversions._
import de.uniulm.iai.comma.lib.ast.javasource.EnhancedCommonTree
import org.antlr.runtime.tree.CommonTree

object TreeWrapper {
  implicit def wrapTreeNode(node: EnhancedCommonTree) = new TreeNode(node)
}

/**
 * Antlr happily returns null in case a node has no children. This class wraps the default antlr tree nodes
 * to return a scala option instead.
 *
 * @param node
 */
class TreeNode(node: EnhancedCommonTree) extends CommonTree {

  def childAt(index: Int): Option[EnhancedCommonTree] = {
    if (node.getChildCount >= (index + 1)) Some(node.getChild(index)) else None
  }

  def findChildNode(nodeType: Int): Option[EnhancedCommonTree] = {
    if (node.getChildCount > 0) node.getChildren.find(_.getType == nodeType) else None
  }
}
