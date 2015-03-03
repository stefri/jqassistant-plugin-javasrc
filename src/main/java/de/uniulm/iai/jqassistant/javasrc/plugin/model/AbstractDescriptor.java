package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * @author Steffen Kram
 */
public interface AbstractDescriptor {

    @Property("abstract")
    Boolean isAbstract();

    void setAbstract(Boolean isAbstract);
}
