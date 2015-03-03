package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import de.uniulm.iai.jqassistant.javasrc.plugin.api.annotation.Declares;

/**
 * Created by steffen on 02.03.15.
 */
public interface DeclaratorDescriptor extends JavaSourceDescriptor, Descriptor {
    @Relation.Incoming
    @Declares
    JavaSourceDescriptor getDeclaringType();

    void setDeclaringType(JavaSourceDescriptor parent);
}
