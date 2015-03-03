package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * @author Steffen Kram
 */
@Relation("READS")
public interface ReadsDescriptor extends Descriptor, LineNumberDescriptor {

    @Relation.Outgoing
    FunctionDescriptor getFunction();

    @Relation.Incoming
    FieldDescriptor getField();

}
