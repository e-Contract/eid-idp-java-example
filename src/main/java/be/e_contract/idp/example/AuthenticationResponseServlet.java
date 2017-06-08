/*
 * eID Identity Provider Java Example.
 *
 * Copyright 2015 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.idp.example;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.pape.PapeResponse;

@WebServlet(AuthenticationResponseServlet.URL_PATTERN)
public class AuthenticationResponseServlet extends HttpServlet {

    public static final String URL_PATTERN = "/openid-authn-response";

    private static final Log LOG = LogFactory.getLog(AuthenticationResponseServlet.class);

    @Inject
    private ExampleController demoController;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOG.debug("id_res");
        request.setCharacterEncoding("UTF8");
        response.setCharacterEncoding("UTF8");

        ParameterList parameterList = new ParameterList(request.getParameterMap());
        DiscoveryInformation discoveryInformation = AuthenticationRequestServlet.getDiscoveryInformation(request);
        String requestReturnTo = AuthenticationRequestServlet.getReturnTo(request);
        ConsumerManager consumerManager = AuthenticationRequestServlet
                .getConsumerManager(request);
        VerificationResult verificationResult;
        try {
            verificationResult = consumerManager.verify(
                    requestReturnTo, parameterList, discoveryInformation);
        } catch (MessageException ex) {
            LOG.error("OpenID message error: " + ex.getMessage(), ex);
            return;
        } catch (DiscoveryException ex) {
            LOG.error("OpenID discovery error: " + ex.getMessage(), ex);
            return;
        } catch (AssociationException ex) {
            LOG.error("OpenID association error: " + ex.getMessage(), ex);
            return;
        }
        Identifier identifier = verificationResult.getVerifiedId();
        if (null != identifier) {
            LOG.debug("OpenID identifier: " + identifier.getIdentifier());

            List<Attribute> attributes = new LinkedList<>();
            String authnPolicy = null;

            Message authResponse = verificationResult.getAuthResponse();
            if (authResponse.hasExtension(AxMessage.OPENID_NS_AX)) {
                MessageExtension messageExtension;
                try {
                    messageExtension = authResponse
                            .getExtension(AxMessage.OPENID_NS_AX);
                } catch (MessageException ex) {
                    LOG.error("OpenID message error: " + ex.getMessage(), ex);
                    return;
                }
                if (messageExtension instanceof FetchResponse) {
                    FetchResponse fetchResponse = (FetchResponse) messageExtension;
                    Map<String, String> attributeTypes = fetchResponse
                            .getAttributeTypes();
                    for (Map.Entry<String, String> entry : attributeTypes
                            .entrySet()) {
                        attributes
                                .add(new Attribute(entry.getValue(), fetchResponse
                                        .getAttributeValue(entry.getKey())));
                    }
                }
            }

            // PAPE
            if (authResponse.hasExtension(PapeResponse.OPENID_NS_PAPE)) {
                MessageExtension messageExtension;
                try {
                    messageExtension = authResponse
                            .getExtension(PapeResponse.OPENID_NS_PAPE);
                } catch (MessageException ex) {
                    LOG.error("OpenID message error: " + ex.getMessage(), ex);
                    return;
                }
                if (messageExtension instanceof PapeResponse) {
                    PapeResponse papeResponse = (PapeResponse) messageExtension;
                    authnPolicy = papeResponse.getAuthPolicies();
                }
            }

            this.demoController.authenticated(identifier.getIdentifier(), attributes, authnPolicy);
            String location = request.getContextPath() + "/authenticated.xhtml";
            response.sendRedirect(location);
        }
    }
}
