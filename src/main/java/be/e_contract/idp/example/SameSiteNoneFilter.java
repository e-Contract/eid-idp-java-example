/*
 * eID Identity Provider Java Example.
 *
 * Copyright 2020 e-Contract.be BV. All rights reserved.
 * e-Contract.be BV proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.idp.example;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet filter that adds the SameSite=None flag to cookies. This is required
 * for the OpenID 2.0 protocol to function correctly under Google Chrome version
 * 80 and higher. Please be aware of the security implications of using this
 * servlet filter.
 *
 * @author Frank Cornelis
 *
 */
public class SameSiteNoneFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(request, new SameSiteNoneHttpServletResponseWrapper((HttpServletResponse) response));
    }

    @Override
    public void destroy() {
    }
}
