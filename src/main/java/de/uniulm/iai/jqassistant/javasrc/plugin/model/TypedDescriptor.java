package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Interface for value descriptors which provide a type information
 *
 * @author Steffen Kram
 */
public interface TypedDescriptor {

    @Relation("OF_TYPE")
    TypeDescriptor getType();

    void setType(TypeDescriptor type);
}
