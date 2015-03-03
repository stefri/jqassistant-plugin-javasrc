package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * @author Steffen Kram
 */
@Label(value = "Anonymous")
public interface AnonymousClassDescriptor extends ClassDescriptor {

    @Property("index")
    Integer getIndex();

    void setIndex(Integer index);
}
