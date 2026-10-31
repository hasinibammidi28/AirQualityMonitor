package com.example.airqualitymonitor.sensor;

import com.example.airqualitymonitor.model.Reading;
import java.util.Random;

public class Sensor extends Thread {

    private String sensorId;
    private String areaName;
    private final Random random = new Random();

    private Reading latestReading;

    public Sensor(String sensorId, String areaName) {
        this.sensorId = sensorId;
        this.areaName = areaName;
    }

    @Override
    public void run() {
        latestReading = generateReading();

        System.out.println(
                "Sensor: " + sensorId
                        + " | Area: " + areaName
                        + " | AQI: " + latestReading.getAqi()
        );
    }

    public Reading generateReading() {
        int aqi = random.nextInt(301);
        return new Reading(areaName, aqi);
    }

    public Reading getLatestReading() {
        return latestReading;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getAreaName() {
        return areaName;
    }
}