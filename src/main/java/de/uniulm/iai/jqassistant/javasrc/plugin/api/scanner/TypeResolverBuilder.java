package de.uniulm.iai.jqassistant.javasrc.plugin.api.scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;

public class TypeResolverBuilder {

    private TypeResolverBuilder() {
    }

    public static TypeResolver createTypeResolver(ScannerContext context) {
        return new DefaultTypeResolver();
    }

}
