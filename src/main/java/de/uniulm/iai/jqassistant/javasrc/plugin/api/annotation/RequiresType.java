package de.uniulm.iai.jqassistant.javasrc.plugin.api.annotation;

import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Steffen Kram
 */
@Relation("REQUIRES")
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresType {
}
