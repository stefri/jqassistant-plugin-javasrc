package de.uniulm.iai.jqassistant.javasrc.plugin.api.scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import de.uniulm.iai.jqassistant.javasrc.plugin.model.JavaCompilationUnitDescriptor;

public class TypeResolverBuilder {

    private TypeResolverBuilder() {
    }

    public static TypeResolver createTypeResolver(ScannerContext context) {
        JavaCompilationUnitDescriptor compilationUnitDescriptor = context.peek(JavaCompilationUnitDescriptor.class);
        if (compilationUnitDescriptor != null) {
            return new ArtifactBasedTypeResolver(compilationUnitDescriptor);
        } else {
            return new DefaultTypeResolver();
        }
    }

}
