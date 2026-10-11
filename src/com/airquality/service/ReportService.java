package com.airquality.service;

import com.airquality.model.Reading;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ReportService {


public void saveReport(List<Reading> readings) {

    try (FileWriter writer =
             new FileWriter("air_quality_report.txt")) {

        writer.write("AIR QUALITY MONITOR REPORT\n");
        writer.write("==========================\n\n");

        for (Reading reading : readings) {

            writer.write(
                "Area: " + reading.getAreaName() + "\n"
            );

            writer.write(
                "AQI: " + reading.getAqi() + "\n"
            );

            writer.write(
                "Level: " + reading.classify() + "\n"
            );

            writer.write(
                "Time: " + reading.getTimestamp() + "\n\n"
            );
        }

        System.out.println(
            "\nReport saved to air_quality_report.txt"
        );

    } catch (IOException e) {

        System.out.println(
            "Could not save the report: " + e.getMessage()
        );
    }
}

}
