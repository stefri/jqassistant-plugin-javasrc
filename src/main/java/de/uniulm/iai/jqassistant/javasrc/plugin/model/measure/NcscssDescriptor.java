package de.uniulm.iai.jqassistant.javasrc.plugin.model.measure;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * @author Steffen Kram
 */
public interface NcscssDescriptor extends MeasureDescriptor {

    @Property("NCSCSS")
    Long getNcscss();

    void setNcscss(Long count);
}
