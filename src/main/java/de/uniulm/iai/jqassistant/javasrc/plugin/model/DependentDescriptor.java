package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

/**
 * @author Steffen Kram
 */
public interface DependentDescriptor extends Descriptor, JavaSourceDescriptor {

    @Relation("DEPENDS_ON")
    List<TypeDescriptor> getDependencies();

}
