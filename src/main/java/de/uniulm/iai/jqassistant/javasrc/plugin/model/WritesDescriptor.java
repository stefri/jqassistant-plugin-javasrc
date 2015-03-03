package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * @author Steffen Kram
 */
@Relation("WRITES")
public interface WritesDescriptor extends Descriptor, LineNumberDescriptor {

    @Outgoing
    FunctionDescriptor getFunction();

    @Incoming
    FieldDescriptor getField();

}
