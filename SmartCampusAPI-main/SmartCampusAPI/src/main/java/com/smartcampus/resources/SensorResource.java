package com.smartcampus.resources;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.service.DataStore;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    @GET
    public Collection<Sensor> getSensors(@QueryParam("type") String type) {
        if (type != null && !type.isEmpty()) {
            return dataStore.getSensors().values().stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return dataStore.getSensors().values();
    }

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        String roomId = sensor.getRoomId();
        Room room = dataStore.getRooms().get(roomId);

        if (room == null) {
            throw new LinkedResourceNotFoundException("Room ID '" + roomId + "' not found.");
        }

        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Bad Request");
            err.put("message", "Sensor ID is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        dataStore.getSensors().put(sensor.getId(), sensor);
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }
        dataStore.getReadings().putIfAbsent(sensor.getId(), new ArrayList<>());

        URI uri = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(uri).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(sensor).build();
    }

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }

        // Unlink from parent room first
        Room room = dataStore.getRooms().get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        dataStore.getSensors().remove(sensorId);
        dataStore.getReadings().remove(sensorId);

        Map<String, String> result = new HashMap<>();
        result.put("message", "Sensor '" + sensorId + "' has been successfully deleted.");
        result.put("status", "200");
        return Response.ok(result).build();
    }

    @PATCH
    @Path("/{sensorId}/status")
    public Response updateSensorStatus(@PathParam("sensorId") String sensorId, Sensor statusUpdate) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        sensor.setStatus(statusUpdate.getStatus());
        return Response.ok(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
