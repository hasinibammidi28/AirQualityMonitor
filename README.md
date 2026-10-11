# Air Quality Monitor and Health Alert System

## Project Description
This Java application monitors simulated air quality readings
for different areas and provides health alerts based on AQI values.

## Features
- Simulated air quality sensors
- Multithreading using Java Thread
- AQI classification
- Health alerts
- Custom exception handling
- ArrayList for storing readings
- Sorting areas by AQI
- File handling to generate reports

## Technologies Used
- Java 21
- Object-Oriented Programming
- VS Code

## AQI Classification
- GOOD: 0–50
- MODERATE: 51–100
- POOR: 101–200
- SEVERE: Above 200

Note: These are simplified educational thresholds, not an official AQI standard.

## How to Run

Compile:
javac -d out (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })

Run:
java -cp out com.airquality.app.Main

## Output
The application displays air quality readings, health alerts,
sorted results, and saves a report to a text file.
