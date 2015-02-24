package com.buschmais.jqassistant.demo.plugin.model;

/**
 * Template interface for all descriptors providing line number information.
 * 
 * @author Dirk Mahler
 */
public interface LineNumberDescriptor {

    int getLineNumber();

    void setLineNumber(int lineNumber);

}
