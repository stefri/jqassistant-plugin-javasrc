package de.uniulm.iai.jqassistant.javasrc.plugin.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Defines the scopes for java sources.
 */
public enum JavaSourceScope implements Scope {

    CLASSPATH {
        @Override
        public void create(ScannerContext context) {
            context.push(TypeResolver.class, TypeResolverBuilder.createTypeResolver(context));
        }

        @Override
        public void destroy(ScannerContext context) {
            context.pop(TypeResolver.class);
        }
    };

    @Override
    public String getPrefix() {
        return "javasrc";
    }

    @Override
    public String getName() {
        return name();
    }
}
