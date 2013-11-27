package com.xoom.oss.fs;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

public class AccessLoggingFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (System.getProperty("access.logging") != null) {
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            StringBuilder sb = new StringBuilder()
                    .append(new Date().toString())
                    .append(" ")
                    .append(servletRequest.getRemoteHost())
                    .append(" ")
                    .append(servletRequest.getProtocol())
                    .append(" ")
                    .append(servletRequest.getMethod())
                    .append(" ")
                    .append(servletRequest.getRequestURI());
            System.out.printf("%s\n", sb.toString());
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
