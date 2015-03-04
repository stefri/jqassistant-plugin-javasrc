package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;
import de.uniulm.iai.jqassistant.javasrc.plugin.api.annotation.Declares;
import de.uniulm.iai.jqassistant.javasrc.plugin.api.annotation.IsMainType;
import de.uniulm.iai.jqassistant.javasrc.plugin.api.annotation.RequiresType;

import java.util.List;

/**
 * Represents a java compilation unit, meaning one single java source file.
 *
 * @author Steffen Kram
 */
@Label("JavaCompilationUnit")
public interface JavaCompilationUnitDescriptor extends ArtifactFileDescriptor, JavaSourceDescriptor {

    @Property("mayCompile")
    Boolean isMayCompile();

    void setMayCompile(Boolean mayCompile);

    @Outgoing
    @IsMainType
    TypeDescriptor getMainType();

    void setMainType(TypeDescriptor mainType);

    @Outgoing
    @Declares
    List<TypeDescriptor> getDeclaredTypes();

    @ResultOf
    @Cypher("match (type:Type)<-[:CONTAINS]-(a:Artifact) where type.fqn={fqn} and id(a) in {dependencies} return type")
    Query.Result<TypeDescriptor> resolveRequiredType(@Parameter("fqn") String fqn, @Parameter("dependencies") List<?
            extends ArtifactDescriptor> dependencies);

    /**
     * Return the list of java types required by this artifact (i.e. which are
     * referenced from it).
     *
     * @return The list of required java types.
     */
    @Outgoing
    @RequiresType
    List<TypeDescriptor> getRequiresTypes();

}
