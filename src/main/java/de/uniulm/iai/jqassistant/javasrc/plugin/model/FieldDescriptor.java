package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

import java.util.List;

/**
 * @author Steffen Kram
 */
@Label("Field")
public interface FieldDescriptor extends MemberDescriptor, NamedDescriptor, TypedDescriptor, AccessModifierDescriptor,
        LineNumberDescriptor {

    @Property("transient")
    Boolean isTransient();

    void setTransient(Boolean isTransient);


    @Property("volatile")
    Boolean isVolatile();

    void setVolatile(Boolean isVolatile);


    List<WritesDescriptor> getWrittenBy();

    List<ReadsDescriptor> getReadBy();

}
