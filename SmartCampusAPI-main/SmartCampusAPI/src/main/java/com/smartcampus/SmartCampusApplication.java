package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * JAX-RS Application entry point.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {
    public SmartCampusApplication() {
        // Auto-scan and register all resource, exception mapper, and filter classes
        packages("com.smartcampus.resources", "com.smartcampus.exception", "com.smartcampus.filters");
    }
}
