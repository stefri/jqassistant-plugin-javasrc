package com.buschmais.jqassistant.demo.plugin.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
//import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Represents a marker label for all elements of "MyLanguage".
 * 
 * @author Dirk Mahler
 */
//@Abstract
@Label("MyLanguage")
public interface MyLanguageDescriptor extends Descriptor {
}
