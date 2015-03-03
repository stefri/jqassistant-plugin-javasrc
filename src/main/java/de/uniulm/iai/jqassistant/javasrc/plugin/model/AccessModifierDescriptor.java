package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Interface to describe the access to java elements.
 *
 * @author Steffen Kram
 */
public interface AccessModifierDescriptor {

    @Property("visibility")
    String getVisibility();

    void setVisibility(String visibility);


    @Property("static")
    Boolean isStatic();

    void setStatic(Boolean isStatic);


    @Property("final")
    Boolean isFinal();

    void setFinal(Boolean isFinal);

}
