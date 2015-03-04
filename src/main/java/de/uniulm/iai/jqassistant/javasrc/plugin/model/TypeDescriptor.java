package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;
import de.uniulm.iai.jqassistant.javasrc.plugin.api.annotation.Declares;
import de.uniulm.iai.jqassistant.javasrc.plugin.api.annotation.RequiresType;

import java.util.List;

/**
 * @author Steffen Kram
 */
@Label(value = "Type", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface TypeDescriptor extends FullQualifiedNameDescriptor, NamedDescriptor, BlockLineSpanDescriptor,
        AccessModifierDescriptor, AbstractDescriptor, DeclaratorDescriptor, DependentDescriptor, JavaSourceDescriptor {

    /**
     * Return the super class.
     *
     * @return The super class.
     */
    @Relation("EXTENDS")
    TypeDescriptor getSuperClass();

    /**
     * Set the super class.
     *
     * @param superClass
     *            The super class.
     */
    void setSuperClass(TypeDescriptor superClass);

    /**
     * Return the implemented interfaces.
     *
     * @return The implemented interfaces.
     */
    @Relation("IMPLEMENTS")
    List<TypeDescriptor> getInterfaces();

    /**
     * Return the declared constructors.
     *
     * @return The declared constructors.
     */
    @Outgoing
    @Declares
    List<ConstructorDescriptor> getDeclaredConstructors();

    /**
     * Return the declared methods.
     *
     * @return The declared methods.
     */
    @Outgoing
    @Declares
    List<MethodDescriptor> getDeclaredMethods();

    /**
     * Return the declared fields.
     *
     * @return The declared fields.
     */
    @Outgoing
    @Declares
    List<FieldDescriptor> getDeclaredFields();

    /**
     * Return the declared members, i.e. fields and methods.
     *
     * @return The declared members.
     */
    @Outgoing
    @Declares
    List<MemberDescriptor> getDeclaredMembers();

    /**
     * Return the declared inner classes.
     *
     * @return The declared inner classes.
     */
    @Relation("DECLARES")
    List<TypeDescriptor> getDeclaredInnerTypes();


    @Incoming
    @Declares
    JavaCompilationUnitDescriptor getDeclarationUnit();

    void setDeclarationUnit(JavaCompilationUnitDescriptor compilationUnit);


    @Incoming
    @RequiresType
    JavaCompilationUnitDescriptor getRequiredBy();

    void setRequiredBy(JavaCompilationUnitDescriptor compilationUnit);

}
