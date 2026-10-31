package com.example.airqualitymonitor.service;

import com.example.airqualitymonitor.exception.HazardousAirException;
import com.example.airqualitymonitor.model.Area;
import com.example.airqualitymonitor.model.Reading;
import com.example.airqualitymonitor.model.AirQualityLevel;
import com.example.airqualitymonitor.sensor.Sensor;

import android.util.Log;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class AirQualityMonitor {

    private final ArrayList<Area> areas = new ArrayList<>();
    private final ArrayList<Reading> readings = new ArrayList<>();

    private final AlertService alertService = new AlertService();
    private final ReportService reportService = new ReportService();

    public void registerArea(Area area) {
        areas.add(area);
    }

    public void generateReadings() {

        readings.clear();

        ArrayList<Sensor> sensors = new ArrayList<>();

        // Create a new thread for every registered area.
        for (Area area : areas) {
            Sensor original = area.getSensor();

            Sensor sensor = new Sensor(
                    original.getSensorId(),
                    original.getAreaName()
            );

            sensors.add(sensor);
            sensor.start();
        }

        // Wait for all sensor threads to finish.
        for (Sensor sensor : sensors) {
            try {
                sensor.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // Collect readings after the threads finish.
        for (Sensor sensor : sensors) {

            Reading reading = sensor.getLatestReading();

            if (reading == null) {
                continue;
            }

            readings.add(reading);

            AirQualityLevel level = reading.classify();

            if (level == AirQualityLevel.POOR ||
                    level == AirQualityLevel.SEVERE) {

                alertService.sendAlert(
                        reading.getAreaName(),
                        reading.getAqi(),
                        level
                );
            }

            try {
                checkHazardousAir(reading);
            } catch (HazardousAirException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void checkHazardousAir(Reading reading)
            throws HazardousAirException {

        if (reading.getAqi() > 200) {
            throw new HazardousAirException(
                    "Hazardous AQI detected in "
                            + reading.getAreaName()
                            + ". AQI: " + reading.getAqi()
            );
        }
    }

    public String compareAreas() {
        return reportService.generateReport(readings);
    }

    public ArrayList<Reading> getReadings() {
        return new ArrayList<>(readings);
    }

    public ArrayList<Area> getAreas() {
        return new ArrayList<>(areas);
    }
    public boolean saveReportToFile(File file) {
        String report = reportService.generateReport(readings);

        try (java.io.FileWriter writer =
                     new java.io.FileWriter(file)) {

            writer.write(report);

            Log.d("AirQualityMonitor",
                    "Report saved to: " + file.getAbsolutePath());

            return true;

        } catch (java.io.IOException e) {

            Log.e("AirQualityMonitor",
                    "Failed to save report", e);

            return false;
        }
    }
}