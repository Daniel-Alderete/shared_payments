package com.practice.shared_payment_backend.configuration.markers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

@Component
@Order(1)
public class MDCFilter extends OncePerRequestFilter {
    public static final String REQUEST_HEADER = "X-Correlator-Id";
    public static final String MDC_TOKEN_KEY = "RequestId";
    protected Logger logger = LoggerFactory.getLogger(MDCFilter.class);

    public MDCFilter() {
        super();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            final String token = getMarker(request);
            MDC.put(MDC_TOKEN_KEY, token);

            if (hasText(REQUEST_HEADER)) {
                response.addHeader(REQUEST_HEADER, token);
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TOKEN_KEY);
        }
    }

    private String getMarker(final HttpServletRequest request) {
        final String token;

        if (hasText(REQUEST_HEADER) && hasText(request.getHeader(REQUEST_HEADER))) {
            token = request.getHeader(REQUEST_HEADER);
        } else {
            token = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        }

        logger.trace("Marker id is: {}", token);

        return token;
    }
}
