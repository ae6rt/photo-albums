package org.petrovic.photos;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AccessLoggingFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (System.getProperty("access.logging") != null) {
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            StringBuilder sb = new StringBuilder()
                    .append(servletRequest.getRemoteHost())
                    .append(" ")
                    .append(servletRequest.getProtocol())
                    .append(" ")
                    .append(servletRequest.getMethod())
                    .append(" ")
                    .append(servletRequest.getRequestURI());
            String queryString = servletRequest.getQueryString();
            if (queryString != null) {
                sb.append("?").append(queryString);
            }
            System.out.printf("%s\n", sb.toString());
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
