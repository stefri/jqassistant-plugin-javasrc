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

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.Tree;

import java.util.List;

public class EnhancedCommonErrorTree extends EnhancedCommonTree {

    private final CommonErrorNode errorNode;

    public EnhancedCommonErrorTree(TokenStream input, Token start, Token stop, RecognitionException e) {
        super(start);
        this.errorNode = new CommonErrorNode(input, start, stop, e);
    }

    public EnhancedCommonTree getChild(int i) {
        return (EnhancedCommonTree) errorNode.getChild(i);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List getChildren() {
        return errorNode.getChildren();
    }

    @Override
    public boolean isNil() {
        return errorNode.isNil();
    }

    @Override
    public int getType() {
        return errorNode.getType();
    }

    @Override
    public String getText() {
        return errorNode.getText();
    }

    @Override
    public Tree getFirstChildWithType(int type) {
        return errorNode.getFirstChildWithType(type);
    }

    @Override
    public Token getToken() {
        return errorNode.getToken();
    }

    @Override
    public Tree dupNode() {
        return errorNode.dupNode();
    }

    @Override
    public void addChild(Tree t) {
        errorNode.addChild(t);
    }

    @Override
    public String toString() {
        return errorNode.toString();
    }

    @Override
    public int getCharPositionInLine() {
        return errorNode.getCharPositionInLine();
    }

    @Override
    public int getTokenStartIndex() {
        return errorNode.getTokenStartIndex();
    }

    @Override
    public void setTokenStartIndex(int index) {
        errorNode.setTokenStartIndex(index);
    }

    @Override
    public int getTokenStopIndex() {
        return errorNode.getTokenStopIndex();
    }

    @Override
    public void setTokenStopIndex(int index) {
        errorNode.setTokenStopIndex(index);
    }

    @Override
    public void setUnknownTokenBoundaries() {
        errorNode.setUnknownTokenBoundaries();
    }

    @Override
    public void addChildren(List<? extends Tree> kids) {
        errorNode.addChildren(kids);
    }

    @Override
    public void setChild(int i, Tree t) {
        errorNode.setChild(i, t);
    }

    @Override
    public void setParent(Tree t) {
        errorNode.setParent(t);
    }

    @Override
    public int getChildCount() {
        return errorNode.getChildCount();
    }

    @Override
    public int getLine() {
        return errorNode.getLine();
    }

    @Override
    public int getChildIndex() {
        return errorNode.getChildIndex();
    }

    @Override
    public EnhancedCommonTree getParent() {
        return (EnhancedCommonTree) errorNode.getParent();
    }

    @Override
    public void freshenParentAndChildIndexes() {
        errorNode.freshenParentAndChildIndexes();
    }

    @Override
    public void freshenParentAndChildIndexes(int offset) {
        errorNode.freshenParentAndChildIndexes(offset);
    }

    @Override
    public void freshenParentAndChildIndexesDeeply() {
        errorNode.freshenParentAndChildIndexesDeeply();
    }

    @Override
    public void freshenParentAndChildIndexesDeeply(int offset) {
        errorNode.freshenParentAndChildIndexesDeeply(offset);
    }

    @Override
    public void sanityCheckParentAndChildIndexes() {
        errorNode.sanityCheckParentAndChildIndexes();
    }

    @Override
    public void sanityCheckParentAndChildIndexes(Tree parent, int i) {
        errorNode.sanityCheckParentAndChildIndexes(parent, i);
    }

    @Override
    public boolean hasAncestor(int ttype) {
        return errorNode.hasAncestor(ttype);
    }

    @Override
    public Tree getAncestor(int ttype) {
        return errorNode.getAncestor(ttype);
    }

    @Override
    public List<? extends Tree> getAncestors() {
        return errorNode.getAncestors();
    }

    @Override
    public String toStringTree() {
        return errorNode.toStringTree();
    }

    @Override
    public int hashCode() {
        return errorNode.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return errorNode.equals(obj);
    }

    @Override
    public void insertChild(int i, Object t) {
        errorNode.insertChild(i, t);
    }

    @Override
    public Object deleteChild(int i) {
        return errorNode.deleteChild(i);
    }

    @Override
    public void replaceChildren(int startChildIndex, int stopChildIndex, Object t) {
        errorNode.replaceChildren(startChildIndex, stopChildIndex, t);
    }

    @Override
    public void setChildIndex(int index) {
        errorNode.setChildIndex(index);
    }
}

