package de.uniulm.iai.jqassistant.javasrc.plugin.model.measure;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * @author Steffen Kram
 */
public interface CyclomaticComplexityDescriptor extends MeasureDescriptor {

    @Property("CCN")
    Long getCyclomaticComplexityNumber();

    void setCyclomaticComplexityNumber(Long count);
}
