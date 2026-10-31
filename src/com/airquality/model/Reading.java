package com.airquality.model;

import java.time.LocalDateTime;

public class Reading {

private String areaName;
private int aqi;
private LocalDateTime timestamp;

public Reading(String areaName, int aqi) {
    this.areaName = areaName;
    this.aqi = aqi;
    this.timestamp = LocalDateTime.now();
}

public String getAreaName() {
    return areaName;
}

public int getAqi() {
    return aqi;
}

public LocalDateTime getTimestamp() {
    return timestamp;
}

public AirQualityLevel classify() {

    if (aqi <= 50) {
        return AirQualityLevel.GOOD;
    } else if (aqi <= 100) {
        return AirQualityLevel.MODERATE;
    } else if (aqi <= 200) {
        return AirQualityLevel.POOR;
    } else {
        return AirQualityLevel.SEVERE;
    }
}

}
