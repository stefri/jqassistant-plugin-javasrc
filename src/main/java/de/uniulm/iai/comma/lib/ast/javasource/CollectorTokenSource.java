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
import org.antlr.runtime.TokenSource;

import java.util.*;

/**
 * This class is modeled after a blog post from Mária Jurčovičová.
 * See http://meri-stuff.blogspot.de/2012/09/tackling-comments-in-antlr-compiler.html for
 * details.
 */
public class CollectorTokenSource implements TokenSource {
    private final TokenSource source;
    private final Set<Integer> collectTokenTypes = new HashSet<>();
    private final List<Token> collectedTokens = new ArrayList<>();

    public CollectorTokenSource(TokenSource source, Collection<Integer> collectTokenTypes) {
        super();
        this.source = source;
        this.collectTokenTypes.addAll(collectTokenTypes);
    }

    /**
     * Returns next token from the wrapped token source. Stores it in a list if necessary.
     */
    @Override
    public Token nextToken() {
        Token nextToken = source.nextToken();
        if (shouldCollect(nextToken)) {
            collectedTokens.add(nextToken);
        }
        return nextToken;
    }

    /**
     * Decide whether collect the token or not.
     */
    protected boolean shouldCollect(Token nextToken) {
        // filter the token by its type
        return collectTokenTypes.contains(nextToken.getType());
    }

    public List<Token> getCollectedTokens() {
        return collectedTokens;
    }


    @Override
    public String getSourceName() {
        return "Collect hidden channel " + source.getSourceName();
    }
}
