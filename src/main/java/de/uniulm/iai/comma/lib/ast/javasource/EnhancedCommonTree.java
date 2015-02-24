/*
 * comma, A Code Measurement and Analysis Tool
 * Copyright (C) 2010-2013 Steffen Kram
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
package de.uniulm.iai.comma.lib.ast.javasource;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public class EnhancedCommonTree extends CommonTree {

    private List<Token> preceding = new ArrayList<Token>();
    private List<Token> following = new ArrayList<Token>();
    private int lastLine = -1;

    public EnhancedCommonTree(Token t) {
        super(t);
    }

    @Override
    public EnhancedCommonTree getChild(int i) {
        return (EnhancedCommonTree) super.getChild(i);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<EnhancedCommonTree> getChildren() {
        List childs = super.getChildren();
        if (childs == null) return null;
        return (List<EnhancedCommonTree>) childs;
    }

    @Override
    public EnhancedCommonTree getParent() {
        return (EnhancedCommonTree) super.getParent();
    }

    public int getLastLine() {
        if ( lastLine >= 0 ) return lastLine; // already set
        if ( children == null ) {
            if ( startIndex < 0 || stopIndex < 0 ) {
                lastLine = token.getLine();
            }
            return lastLine;
        }
        if ( children.size() > 0 ) {
            EnhancedCommonTree lastChild = getChildren().get(children.size()-1);
            lastLine = lastChild.getLine();
        }
        return lastLine;
    }

    public void addPreceding(Collection<Token> tokens) {
        this.preceding.addAll(tokens);
    }

    public List<Token> getPrecedingComments() {
        return this.preceding;
    }

    public void addFollowing(Collection<Token> tokens) {
        this.following.addAll(tokens);
    }

    public List<Token> getFollowingComments() {
        return this.following;
    }
}
