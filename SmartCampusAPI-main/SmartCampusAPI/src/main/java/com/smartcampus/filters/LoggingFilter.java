package com.smartcampus.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * API Logging Filter — implements cross-cutting observability.
 * Using a JAX-RS filter for logging is far superior to manually inserting
 * Logger.info() calls inside every resource method:
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("[REQUEST]  " + requestContext.getMethod()
                + " " + requestContext.getUriInfo().getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info("[RESPONSE] Status: " + responseContext.getStatus()
                + " for " + requestContext.getMethod()
                + " " + requestContext.getUriInfo().getRequestUri());
    }
}
