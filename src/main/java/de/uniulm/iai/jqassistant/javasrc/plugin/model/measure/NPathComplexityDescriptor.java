package de.uniulm.iai.jqassistant.javasrc.plugin.model.measure;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * @author Steffen Kram
 */
public interface NPathComplexityDescriptor extends MeasureDescriptor {

    @Property("NPath")
    Long getNPathComplexity();

    void setNPathComplexity(Long count);
}
