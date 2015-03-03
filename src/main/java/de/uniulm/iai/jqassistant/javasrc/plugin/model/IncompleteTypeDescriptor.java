package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Java source code provides type information, however without all dependencies
 * it's not possible to add exact type information for all types. This interfaces
 * provides an incomplete type label to indicate missing type information.
 *
 * @author Steffen Kram
 */
@Label(value = "IncompleteTypeInformation")
public interface IncompleteTypeDescriptor extends TypeDescriptor {
}
