package de.uniulm.iai.jqassistant.javasrc.plugin.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * @author Steffen Kram
 */
public interface MemberDescriptor extends DeclaratorDescriptor {

    @Property("signature")
    String getSignature();

    void setSignature(String signature);

}
