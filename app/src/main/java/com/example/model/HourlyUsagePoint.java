package com.example.model;

public class HourlyUsagePoint {
    private final int hour; // 0 to 23
    private final float value; // e.g., minutes of usage or MB of data
    private final String label; // e.g. "09:00"

    public HourlyUsagePoint(int hour, float value, String label) {
        this.hour = hour;
        this.value = value;
        this.label = label;
    }

    public int getHour() { return hour; }
    public float getValue() { return value; }
    public String getLabel() { return label; }
}
