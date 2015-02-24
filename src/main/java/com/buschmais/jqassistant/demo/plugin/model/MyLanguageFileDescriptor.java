package com.buschmais.jqassistant.demo.plugin.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Represents a file of "My Language".
 * 
 * @author Dirk Mahler
 */
public interface MyLanguageFileDescriptor extends FileDescriptor, MyLanguageDescriptor {

    @Relation("HAS_LINE")
    List<LineDescriptor> getLines();
}
