package com.smartcampus.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/chaos")
@Produces(MediaType.APPLICATION_JSON)
public class ChaosResource {

    @GET
    @Path("/trigger-500")
    public String triggerError() {
        // Intentionally throws an unchecked exception to demonstrate the GlobalExceptionMapper
        throw new RuntimeException("Simulated internal server error for demo purposes.");
    }
}
