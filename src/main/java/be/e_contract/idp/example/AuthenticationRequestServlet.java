/*
 * eID Identity Provider Java Example.
 * 
 * Copyright 2015 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.idp.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.Discovery;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.html.HtmlResolver;
import org.openid4java.discovery.xri.XriResolver;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.server.IncrementalNonceGenerator;
import org.openid4java.server.NonceGenerator;
import org.openid4java.server.RealmVerifierFactory;
import org.openid4java.util.HttpFetcherFactory;

@WebServlet(AuthenticationRequestServlet.URL_PATTERN)
public class AuthenticationRequestServlet extends HttpServlet {

    public static final String URL_PATTERN = "/openid-authn-request";

    private static final Log LOG = LogFactory.getLog(AuthenticationRequestServlet.class);

    /**
     * We could use CDI scopes here, but we want to restrict the OpenID servlets
     * to plain servlet API as much as possible.
     */
    private static final String CONSUMER_MANAGER_ATTRIBUTE = AuthenticationRequestServlet.class.getName() + ".consumerManager";

    private static final String DISCOVERY_INFORMATION_SESSION_ATTRIBUTE = AuthenticationRequestServlet.class.getName() + ".discoveryInformation";

    private static final String RETURN_TO_SESSION_ATTRIBUTE = AuthenticationRequestServlet.class.getName() + ".returnTo";

    @Inject
    private ExampleController demoController;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        try {
            initConsumerManager(servletConfig);
        } catch (Exception e) {
            LOG.error("openid4j init error: " + e.getMessage(), e);
            throw new ServletException(e);
        }
    }

    private void initConsumerManager(ServletConfig servletConfig) throws NoSuchAlgorithmException, KeyManagementException {
        ServletContext servletContext = servletConfig.getServletContext();
        if (null != servletContext.getAttribute(CONSUMER_MANAGER_ATTRIBUTE)) {
            return;
        }
        HttpFetcherFactory httpFetcherFactory = new HttpFetcherFactory();
        YadisResolver yadisResolver = new YadisResolver(
                httpFetcherFactory);
        RealmVerifierFactory realmFactory = new RealmVerifierFactory(
                yadisResolver);
        HtmlResolver htmlResolver = new HtmlResolver(
                httpFetcherFactory);
        XriResolver xriResolver = Discovery.getXriResolver();
        Discovery discovery = new Discovery(htmlResolver,
                yadisResolver, xriResolver);

        ConsumerManager consumerManager = new ConsumerManager(realmFactory, discovery, httpFetcherFactory);
        servletContext.setAttribute(CONSUMER_MANAGER_ATTRIBUTE, consumerManager);
    }

    public static ConsumerManager getConsumerManager(HttpServletRequest request) {
        ServletContext servletContext = request.getServletContext();
        ConsumerManager consumerManager = (ConsumerManager) servletContext.getAttribute(CONSUMER_MANAGER_ATTRIBUTE);
        return consumerManager;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOG.debug("doGet");
        String type = request.getParameter("type");
        ConsumerManager consumerManager = getConsumerManager(request);
        String userIdentifier = "https://www.e-contract.be/eid-idp/endpoints/openid/" + type;
        List discoveries;
        try {
            discoveries = consumerManager.discover(userIdentifier);
        } catch (DiscoveryException e) {
            LOG.error("OpenID discovery error: " + e.getMessage(), e);
            displayErrorMessage("OpenID discovery error", response);
            return;
        }

        DiscoveryInformation discoveryInformation = consumerManager.associate(discoveries);
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute(DISCOVERY_INFORMATION_SESSION_ATTRIBUTE, discoveryInformation);

        NonceGenerator consumerNonceGenerator = new IncrementalNonceGenerator();
        String csrfNonce = consumerNonceGenerator.next();
        String returnToUrl = request.getScheme() + "://" + request.getServerName();
        switch (request.getScheme()) {
            case "http":
                if (request.getServerPort() != 80) {
                    returnToUrl += ":" + request.getServerPort();
                }
                break;
            case "https":
                if (request.getServerPort() != 443) {
                    returnToUrl += ":" + request.getServerPort();
                }
                break;
        }
        returnToUrl += request.getContextPath()
                + AuthenticationResponseServlet.URL_PATTERN + "?nonce=" + csrfNonce;
        httpSession.setAttribute(RETURN_TO_SESSION_ATTRIBUTE, returnToUrl);

        AuthRequest authRequest;
        try {
            authRequest = consumerManager.authenticate(discoveryInformation, returnToUrl);
        } catch (Exception ex) {
            LOG.error("OpenID authentication request error: " + ex.getMessage(), ex);
            displayErrorMessage("OpenID authentication request error", response);
            return;
        }

        try {
            addAttributes(authRequest, type);
            addUI(authRequest);
        } catch (MessageException ex) {
            LOG.error("OpenID authentication request error: " + ex.getMessage(), ex);
            displayErrorMessage("OpenID authentication request error", response);
            return;
        }

        response.sendRedirect(authRequest.getDestinationUrl(true));
    }

    private void addAttributes(AuthRequest authRequest, String type) throws MessageException {
        FetchRequest fetchRequest = FetchRequest.createFetchRequest();
        switch (type) {
            case "ident":
            case "auth-ident":
                fetchRequest.addAttribute("http://axschema.org/namePerson/first", true);
                fetchRequest.addAttribute("http://openid.net/schema/birthDate/birthMonth", true);
                fetchRequest.addAttribute("http://axschema.org/eid/card-validity/end", true);
                fetchRequest.addAttribute("http://axschema.org/person/gender", true);
                fetchRequest.addAttribute("http://axschema.org/contact/postalAddress/home", true);
                fetchRequest.addAttribute("http://axschema.org/eid/cert/auth", true);
                fetchRequest.addAttribute("http://axschema.org/eid/photo", true);
                fetchRequest.addAttribute("http://axschema.org/eid/card-validity/begin", true);
                fetchRequest.addAttribute("http://axschema.org/contact/city/home", true);
                fetchRequest.addAttribute("http://axschema.org/contact/postalCode/home", true);
                fetchRequest.addAttribute("http://axschema.org/eid/age", true);
                fetchRequest.addAttribute("http://axschema.org/birthDate", true);
                fetchRequest.addAttribute("http://openid.net/schema/birthDate/birthYear", true);
                fetchRequest.addAttribute("http://axschema.org/eid/pob", true);
                fetchRequest.addAttribute("http://axschema.org/eid/card-number", true);
                fetchRequest.addAttribute("http://axschema.org/eid/nationality", true);
                fetchRequest.addAttribute("http://axschema.org/eid/rrn", true);
                fetchRequest.addAttribute("http://openid.net/schema/birthDate/birthday", true);
                fetchRequest.addAttribute("http://axschema.org/namePerson/last", true);
                fetchRequest.addAttribute("http://axschema.org/namePerson", true);
                break;
            case "auth":
                fetchRequest.addAttribute("http://axschema.org/eid/cert/auth", true);
                fetchRequest.addAttribute("http://axschema.org/namePerson/first", true);
                fetchRequest.addAttribute("http://axschema.org/eid/rrn", true);
                fetchRequest.addAttribute("http://axschema.org/namePerson/last", true);
                fetchRequest.addAttribute("http://axschema.org/namePerson", true);
                break;
        }
        authRequest.addExtension(fetchRequest, "ax");
    }

    private void addUI(AuthRequest authRequest) throws MessageException {
        String language = this.demoController.getLanguage();
        if (null == language || language.isEmpty()) {
            return;
        }
        UIMessageExtension uiMessage = new UIMessageExtension();
        uiMessage.setLanguage(language);
        authRequest.addExtension(uiMessage, "ui");
    }

    /**
     * Gives back the OpenID discovery information as was stored within the HTTP
     * session.
     *
     * @param request
     * @return
     */
    public static DiscoveryInformation getDiscoveryInformation(HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        DiscoveryInformation discoveryInformation = (DiscoveryInformation) httpSession.getAttribute(DISCOVERY_INFORMATION_SESSION_ATTRIBUTE);
        return discoveryInformation;
    }

    /**
     * Gives back the return_to as was stored within the HTTP session.
     *
     * @param request
     * @return
     */
    public static String getReturnTo(HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        String returnTo = (String) httpSession.getAttribute(RETURN_TO_SESSION_ATTRIBUTE);
        return returnTo;
    }

    private void displayErrorMessage(String message, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        writer.println(message);
    }
}
