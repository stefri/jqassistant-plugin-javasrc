package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

/**
 * Describes a function of a Java class this might be a method or a constructor.
 *
 * @author Steffen Kram
 */
@Label(value = "Function")
public interface FunctionDescriptor extends MemberDescriptor, NamedDescriptor, AccessModifierDescriptor,
        AbstractDescriptor, BlockLineSpanDescriptor {

    @Relation("HAS")
    List<ParameterDescriptor> getParameters();


    @Relation("RETURNS")
    TypeDescriptor getReturns();

    void setReturns(TypeDescriptor returns);


    @Relation("THROWS")
    TypeDescriptor getDeclaredThrowables();


    List<ReadsDescriptor> getReads();

    List<WritesDescriptor> getWrites();

}
