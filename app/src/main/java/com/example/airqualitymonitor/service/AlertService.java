package com.example.airqualitymonitor.service;

import com.example.airqualitymonitor.model.AirQualityLevel;

public class AlertService {

    public String getHealthAdvice(AirQualityLevel level) {
        switch (level) {
            case GOOD:
                return "Air quality is good. Enjoy your outdoor activities.";

            case MODERATE:
                return "Air quality is acceptable. Sensitive people should take care.";

            case POOR:
                return "Air quality is poor. Reduce prolonged outdoor activities.";

            case SEVERE:
                return "Air quality is severe. Avoid outdoor activities when possible.";

            default:
                return "Air quality information is unavailable.";
        }
    }

    public void sendAlert(String areaName, int aqi, AirQualityLevel level) {
        if (level == AirQualityLevel.POOR ||
                level == AirQualityLevel.SEVERE) {

            System.out.println(
                    "AIR QUALITY ALERT: " + areaName
                            + " | AQI: " + aqi
                            + " | Level: " + level
            );
        }
    }
}