package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;

/**
 * Represents a java source file.
 *
 * @author Steffen Kram
 */
public interface JavaCompilationUnitDescriptor
        extends FileDescriptor, NamedDescriptor, FullQualifiedNameDescriptor, JavaSourceDescriptor {
}
