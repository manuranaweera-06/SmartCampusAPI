package com.smartcampus.resources;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.models.Room;
import com.smartcampus.service.DataStore;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    @GET
    public Collection<Room> getAllRooms() {
        return dataStore.getRooms().values();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room.getId() == null || room.getId().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Bad Request");
            err.put("message", "Room ID is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        dataStore.getRooms().put(room.getId(), room);

        URI uri = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(uri).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        if (room == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Room '" + roomId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        if (room == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Room '" + roomId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }

        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room '" + roomId + "' cannot be deleted. It still has active sensors: " + room.getSensorIds());
        }

        dataStore.getRooms().remove(roomId);
        Map<String, String> result = new HashMap<>();
        result.put("message", "Room '" + roomId + "' has been successfully deleted.");
        result.put("status", "200");
        return Response.ok(result).build();
    }
}
