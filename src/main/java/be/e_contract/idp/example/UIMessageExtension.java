/*
 * eID Identity Provider Java Example.
 *
 * Copyright 2015 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.idp.example;

import org.openid4java.message.MessageExtension;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;

public class UIMessageExtension implements MessageExtension {

    public static final String OPENID_NS_UI = "http://specs.openid.net/extensions/ui/1.0";

    public static final String LANGUAGE_PREFIX = "lang";

    private ParameterList parameters;

    public UIMessageExtension() {
        this.parameters = new ParameterList();
    }

    @Override
    public String getTypeUri() {
        return OPENID_NS_UI;
    }

    @Override
    public ParameterList getParameters() {
        return this.parameters;
    }

    @Override
    public void setParameters(ParameterList parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean providesIdentifier() {
        return false;
    }

    @Override
    public boolean signRequired() {
        return true;
    }

    public void setLanguage(String language) {
        this.parameters.set(new Parameter(LANGUAGE_PREFIX, language));
    }
}
