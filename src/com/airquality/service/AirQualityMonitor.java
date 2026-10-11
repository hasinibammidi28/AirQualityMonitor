package com.airquality.service;

import com.airquality.model.Area;
import com.airquality.model.Reading;
import com.airquality.model.AirQualityLevel;
import com.airquality.sensor.Sensor;
import com.airquality.exception.HazardousAirException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AirQualityMonitor {

private ArrayList<Area> areas = new ArrayList<>();
private ArrayList<Reading> readings = new ArrayList<>();

private AlertService alertService = new AlertService();

// Register an area
public void registerArea(Area area) {
    areas.add(area);
    System.out.println("Area registered: " + area.getName());
}

// Generate readings for all registered areas
public void generateReadings() {

    readings.clear();

    // Start one thread for each registered area
    for (Area area : areas) {
        area.getSensor().start();
    }

    // Wait until all sensor threads finish
    for (Area area : areas) {
        try {
            area.getSensor().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Monitoring was interrupted.");
            return;
        }
    }

    // Collect readings after all threads finish
    for (Area area : areas) {

        Sensor sensor = area.getSensor();
        Reading reading = sensor.getLatestReading();

        readings.add(reading);

        AirQualityLevel level = reading.classify();

        System.out.println("\nArea: " + area.getName());
        System.out.println("Sensor ID: " + sensor.getSensorId());
        System.out.println("AQI: " + reading.getAqi());
        System.out.println("Air Quality: " + level);

        if (level == AirQualityLevel.POOR ||
            level == AirQualityLevel.SEVERE) {

            alertService.sendAlert(
                area.getName(),
                reading.getAqi(),
                level
            );
        }

        try {
            checkHazardousAir(reading);
        } catch (HazardousAirException e) {
            System.out.println("WARNING: " + e.getMessage());
        }
    }
}

// Custom exception handling
private void checkHazardousAir(Reading reading)
        throws HazardousAirException {

    if (reading.getAqi() > 200) {
        throw new HazardousAirException(
            "Hazardous AQI detected in "
            + reading.getAreaName()
            + "! AQI: " + reading.getAqi()
        );
    }
}

// Compare areas and identify the worst area
public void compareAreas() {

    if (readings.isEmpty()) {
        System.out.println("No readings available.");
        return;
    }

    List<Reading> sortedReadings =
        new ArrayList<>(readings);

    sortedReadings.sort(
        Comparator.comparingInt(Reading::getAqi)
    );

    System.out.println("\n===== AREA COMPARISON =====");

    for (Reading reading : sortedReadings) {
        System.out.println(
            reading.getAreaName()
            + " | AQI: " + reading.getAqi()
            + " | Level: " + reading.classify()
        );
    }

    Reading worstArea =
        sortedReadings.get(sortedReadings.size() - 1);

    System.out.println("\n===== WORST AIR QUALITY =====");
    System.out.println("Area: " + worstArea.getAreaName());
    System.out.println("AQI: " + worstArea.getAqi());
    System.out.println("Level: " + worstArea.classify());
}

public ArrayList<Reading> getReadings() {
    return new ArrayList<>(readings);
}

}
