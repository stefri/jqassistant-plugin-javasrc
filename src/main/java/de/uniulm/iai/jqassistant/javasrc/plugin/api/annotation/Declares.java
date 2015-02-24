package de.uniulm.iai.jqassistant.javasrc.plugin.api.annotation;

import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Steffen Kram
 */
@Relation("DECLARES")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Declares {
}
