package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * @author Steffen Kram
 */
public interface LineNumberDescriptor {

    @Property("startingLine")
    int getStartLineNumber();

    void setStartLineNumber(int start);
}
