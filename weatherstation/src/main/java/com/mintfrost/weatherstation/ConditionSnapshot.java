package com.mintfrost.weatherstation;

public class ConditionSnapshot {
    String date;
    String tempValue;
    String humValue;

    public ConditionSnapshot(String date, String tempValue, String humValue) {
        this.date = date;
        this.tempValue = tempValue;
        this.humValue = humValue;
    }

    public String getDate() {
        return date;
    }

    public String getTempValue() {
        return tempValue;
    }

    public String getHumValue() {
        return humValue;
    }
}
