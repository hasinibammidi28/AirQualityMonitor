package com.airquality.app;

import com.airquality.model.Area;
import com.airquality.sensor.Sensor;
import com.airquality.service.AirQualityMonitor;
import com.airquality.service.ReportService;

public class Main {

public static void main(String[] args) {

    AirQualityMonitor monitor =
        new AirQualityMonitor();

    // Create areas and sensors
    Area area1 = new Area(
        "Kukatpally",
        new Sensor("S001", "Kukatpally")
    );

    Area area2 = new Area(
        "Ameerpet",
        new Sensor("S002", "Ameerpet")
    );

    Area area3 = new Area(
        "Gachibowli",
        new Sensor("S003", "Gachibowli")
    );

    Area area4 = new Area(
        "Miyapur",
        new Sensor("S004", "Miyapur")
    );

    Area area5 = new Area(
        "Secunderabad",
        new Sensor("S005", "Secunderabad")
    );

    // Register areas
    monitor.registerArea(area1);
    monitor.registerArea(area2);
    monitor.registerArea(area3);
    monitor.registerArea(area4);
    monitor.registerArea(area5);

    // Generate readings
    System.out.println("\n===== AIR QUALITY READINGS =====");
    monitor.generateReadings();

    // Compare all areas
    monitor.compareAreas();

    // Save report
    ReportService reportService = new ReportService();

    reportService.saveReport(monitor.getReadings());

    System.out.println("\nMonitoring completed.");
}

}
