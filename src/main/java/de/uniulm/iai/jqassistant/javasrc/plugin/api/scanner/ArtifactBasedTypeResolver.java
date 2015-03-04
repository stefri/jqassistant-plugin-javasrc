package de.uniulm.iai.jqassistant.javasrc.plugin.api.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.ResultIterator;
import de.uniulm.iai.jqassistant.javasrc.plugin.model.JavaCompilationUnitDescriptor;
import de.uniulm.iai.jqassistant.javasrc.plugin.model.TypeDescriptor;

/**
 * A type resolver considering an compilationUnit and its optional dependencies as
 * scopes.
 */
class ArtifactBasedTypeResolver extends AbstractTypeResolver {

    private JavaCompilationUnitDescriptor compilationUnit;

    private List<ArtifactDescriptor> dependencies;

    private Map<String, TypeDescriptor> artifactTypes = new HashMap<>();

    /**
     * Constructor.
     *
     * @param compilationUnit
     *            The compilation unit which defines the scope for resolving types.
     */
    ArtifactBasedTypeResolver(JavaCompilationUnitDescriptor compilationUnit) {
        this.compilationUnit = compilationUnit;
        for (FileDescriptor fileDescriptor : compilationUnit.getContains()) {
            if (fileDescriptor instanceof TypeDescriptor) {
                TypeDescriptor typeDescriptor = (TypeDescriptor) fileDescriptor;
                artifactTypes.put(typeDescriptor.getFullQualifiedName(), typeDescriptor);
            }
        }
        for (TypeDescriptor typeDescriptor : compilationUnit.getRequiresTypes()) {
            this.artifactTypes.put(typeDescriptor.getFullQualifiedName(), typeDescriptor);
        }
        this.dependencies = new ArrayList<>();
        for (DependsOnDescriptor dependsOnDescriptor : compilationUnit.getDependencies()) {
            dependencies.add(dependsOnDescriptor.getDependency());
        }
    }

    @Override
    protected TypeDescriptor findInArtifact(String fullQualifiedName, ScannerContext context) {
        return artifactTypes.get(fullQualifiedName);
    }

    @Override
    protected TypeDescriptor findInDependencies(String fullQualifiedName, ScannerContext context) {
        TypeDescriptor typeDescriptor = null;
        if (!dependencies.isEmpty()) {
            Query.Result<TypeDescriptor> typeDescriptors =
                    compilationUnit.resolveRequiredType(fullQualifiedName, dependencies);
            ResultIterator<TypeDescriptor> iterator = typeDescriptors.iterator();
            if (iterator.hasNext()) {
                typeDescriptor = iterator.next();
            }
        }
        return typeDescriptor;
    }

    @Override
    protected void addContainedType(String fqn, TypeDescriptor typeDescriptor) {
        artifactTypes.put(fqn, typeDescriptor);
    }

    @Override
    protected void addRequiredType(String fqn, TypeDescriptor typeDescriptor) {
        artifactTypes.put(fqn, typeDescriptor);
        typeDescriptor.setRequiredBy(compilationUnit);
    }

    @Override
    protected <T extends TypeDescriptor> void removeRequiredType(String fqn, T typeDescriptor) {
        typeDescriptor.setRequiredBy(null);
    }
}
