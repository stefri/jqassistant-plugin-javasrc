package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.xo.neo4j.api.annotation.Property;
import de.uniulm.iai.jqassistant.javasrc.plugin.model.measure.NcscssDescriptor;

/**
 * Template interface for all descriptors providing block line span information.
 *
 * @author Steffen Kram
 */
public interface BlockLineSpanDescriptor extends LineNumberDescriptor, NcscssDescriptor {

    @Property("endingLine")
    int getEndLineNumber();

    void setEndLineNumber(int end);

}
