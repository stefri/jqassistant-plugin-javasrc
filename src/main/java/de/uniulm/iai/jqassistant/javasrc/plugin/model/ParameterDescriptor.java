package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * @author Steffen Kram
 */
@Label(value = "Parameter")
public interface ParameterDescriptor extends JavaSourceDescriptor, TypedDescriptor {
}
