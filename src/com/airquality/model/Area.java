package com.airquality.model;

import com.airquality.sensor.Sensor;

public class Area {


private String name;
private Sensor sensor;

public Area(String name, Sensor sensor) {
    this.name = name;
    this.sensor = sensor;
}

public String getName() {
    return name;
}

public Sensor getSensor() {
    return sensor;
}

}
