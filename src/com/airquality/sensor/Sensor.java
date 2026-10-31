package com.airquality.sensor;

import com.airquality.model.Reading;
import java.util.Random;

public class Sensor extends Thread {

    private String sensorId;
    private String areaName;
    private Random random = new Random();

    private Reading latestReading;

    public Sensor(String sensorId, String areaName) {
        this.sensorId = sensorId;
        this.areaName = areaName;
    }

    @Override
    public void run() {
        latestReading = generateReading();

        System.out.println(
            "Sensor thread: " + sensorId
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