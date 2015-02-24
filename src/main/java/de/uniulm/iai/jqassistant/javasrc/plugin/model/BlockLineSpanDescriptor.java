package de.uniulm.iai.jqassistant.javasrc.plugin.model;

/**
 * Template interface for all descriptors providing block line span information.
 *
 * @author Steffen Kram
 */
public interface BlockLineSpanDescriptor {

    int getStartLineNumber();

    void setStartLineNumber();

    int getEndLineNumber();

    void setEndLineNumber();

}
