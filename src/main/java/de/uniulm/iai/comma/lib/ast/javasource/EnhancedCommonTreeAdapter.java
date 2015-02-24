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
import org.antlr.runtime.tree.CommonTreeAdaptor;

public class EnhancedCommonTreeAdapter extends CommonTreeAdaptor {

    @Override
    public Object create(Token payload) {
        return new EnhancedCommonTree(payload);
    }

    @Override
    public Object errorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
        return new EnhancedCommonErrorTree(input, start, stop, e);
    }

    @Override
    public Object dupNode(Object t) {
        if (t == null) {
            return null;
        }
        return create(((EnhancedCommonTree) t).token);
    }

}
