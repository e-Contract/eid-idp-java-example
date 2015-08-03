/*
 * eID Identity Provider Java Example.
 * 
 * Copyright 2015 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.idp.example;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("idpExampleController")
@SessionScoped
public class ExampleController implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleController.class);

    private String language;

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return this.language;
    }

    private String userId;

    private List<Attribute> attributes;

    private String policy;

    public String getUserId() {
        return this.userId;
    }

    public void authenticated(String userId, List<Attribute> attributes, String policy) {
        this.userId = userId;
        this.attributes = attributes;
        this.policy = policy;
    }

    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    private void reset() {
        this.userId = null;
        this.attributes = null;
        this.policy = null;
    }

    public void openIDAuthentication(String flavor) {
        reset();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        String redirectUrl = externalContext.getRequestContextPath() + AuthenticationRequestServlet.URL_PATTERN
                + "?type=" + flavor;
        LOGGER.debug("redirect URL: {}", redirectUrl);
        try {
            externalContext.redirect(redirectUrl);
        } catch (IOException ex) {
            LOGGER.error("redirect error: " + ex.getMessage(), ex);
        }
    }

    public String getPolicy() {
        return this.policy;
    }

    public List<SelectItem> getLanguages() {
        List<SelectItem> selectItems = new LinkedList<>();
        selectItems.add(new SelectItem("", "-none-"));
        selectItems.add(new SelectItem("nl", "Nederlands"));
        selectItems.add(new SelectItem("en", "English"));
        selectItems.add(new SelectItem("fr", "Francais"));
        return selectItems;
    }
}
