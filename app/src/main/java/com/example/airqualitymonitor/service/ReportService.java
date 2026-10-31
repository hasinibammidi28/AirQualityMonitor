package com.example.airqualitymonitor.service;

import com.example.airqualitymonitor.model.Reading;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class ReportService {

    // Sort readings from highest AQI to lowest AQI.
    public ArrayList<Reading> sortReadings(
            ArrayList<Reading> readings) {

        ArrayList<Reading> sortedReadings =
                new ArrayList<>(readings);

        sortedReadings.sort(
                Comparator.comparingInt(Reading::getAqi)
                        .reversed()
        );

        return sortedReadings;
    }

    // Find the area with the highest AQI.
    public Reading getWorstArea(
            ArrayList<Reading> readings) {

        if (readings == null || readings.isEmpty()) {
            return null;
        }

        return readings.stream()
                .max(Comparator.comparingInt(Reading::getAqi))
                .orElse(null);
    }

    // Generate a readable report.
    public String generateReport(
            ArrayList<Reading> readings) {

        if (readings == null || readings.isEmpty()) {
            return "No air-quality readings available.";
        }

        ArrayList<Reading> sortedReadings =
                sortReadings(readings);

        StringBuilder report = new StringBuilder();

        report.append("AIR QUALITY MONITOR REPORT\n");
        report.append("==========================\n\n");

        for (Reading reading : sortedReadings) {

            report.append("Area: ")
                    .append(reading.getAreaName())
                    .append("\n");

            report.append("AQI: ")
                    .append(reading.getAqi())
                    .append("\n");

            report.append("Status: ")
                    .append(reading.classify())
                    .append("\n");

            report.append("--------------------------\n");
        }

        Reading worstArea = getWorstArea(readings);

        report.append("\nWORST AIR QUALITY AREA\n");
        report.append("Area: ")
                .append(worstArea.getAreaName())
                .append("\n");

        report.append("AQI: ")
                .append(worstArea.getAqi())
                .append("\n");

        return report.toString();
    }

    // Save the report to a text file.
    public boolean saveReportToFile(
            ArrayList<Reading> readings,
            String filePath) {

        try (FileWriter writer = new FileWriter(filePath)) {

            writer.write(generateReport(readings));
            return true;

        } catch (IOException e) {

            System.err.println(
                    "Unable to save report: " + e.getMessage()
            );

            return false;
        }
    }
}