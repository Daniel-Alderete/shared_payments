package com.practice.shared_payment_backend.configuration.auth;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class ApiBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    public static final String SHARED_PAYMENTS = "Shared Payments";
    private static final String HTTP_STATUS_401_MESSAGE = "HTTP Status 401 - ";
    private static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
    private static final String BASIC_REALM_HEADER_MESSAGE = "Basic realm=\"%s\"";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authEx) throws IOException {
        response.addHeader(WWW_AUTHENTICATE_HEADER, String.format(BASIC_REALM_HEADER_MESSAGE, getRealmName()));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = response.getWriter();
        writer.println(HTTP_STATUS_401_MESSAGE + authEx.getMessage());
    }

    @Override
    public void afterPropertiesSet() {
        setRealmName(SHARED_PAYMENTS);
        super.afterPropertiesSet();
    }
}
