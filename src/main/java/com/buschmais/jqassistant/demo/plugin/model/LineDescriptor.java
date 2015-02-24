package com.buschmais.jqassistant.demo.plugin.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Represents a line.
 * 
 * @author Dirk Mahler
 */
@Label("Line")
public interface LineDescriptor extends MyLanguageDescriptor, LineNumberDescriptor {

    @Relation("HAS_NEXT")
    LineDescriptor getNext();

    void setNext(LineDescriptor nextDescriptor);

    String getValue();

    void setValue(String line);

}
