package com.smartcampus.service;

import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataStore handles in-memory storage using thread-safe maps.
 */
public class DataStore {
    private static DataStore instance;

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
        initializeData();
    }

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    private void initializeData() {
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        rooms.put(r1.getId(), r1);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        sensors.put(s1.getId(), s1);
        r1.getSensorIds().add(s1.getId());

        readings.put(s1.getId(), new ArrayList<>());
    }

    public Map<String, Room> getRooms() { return rooms; }
    public Map<String, Sensor> getSensors() { return sensors; }
    public Map<String, List<SensorReading>> getReadings() { return readings; }
}
