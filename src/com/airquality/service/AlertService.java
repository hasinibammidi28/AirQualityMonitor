package com.airquality.service;

import com.airquality.model.AirQualityLevel;

public class AlertService {


public String generateAdvice(AirQualityLevel level) {

    switch (level) {

        case GOOD:
            return "Air quality is satisfactory.";

        case MODERATE:
            return "Sensitive individuals should monitor their symptoms.";

        case POOR:
            return "Reduce prolonged outdoor exertion.";

        case SEVERE:
            return "Avoid outdoor exertion and follow local health advisories.";

        default:
            return "Air quality information unavailable.";
    }
}

public void sendAlert(String areaName, int aqi,
                      AirQualityLevel level) {

    System.out.println("\n--- AIR QUALITY ALERT ---");
    System.out.println("Area: " + areaName);
    System.out.println("AQI: " + aqi);
    System.out.println("Level: " + level);
    System.out.println("Advice: " + generateAdvice(level));
}


}
