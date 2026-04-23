package com.smartcampus.resources;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;
import com.smartcampus.service.DataStore;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        if (!dataStore.getSensors().containsKey(sensorId)) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        List<SensorReading> history = dataStore.getReadings().getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(history).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor '" + sensorId + "' is under maintenance and cannot accept readings.");
        }

        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        dataStore.getReadings().computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        // Side effect: update the parent sensor's currentValue for data consistency
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
