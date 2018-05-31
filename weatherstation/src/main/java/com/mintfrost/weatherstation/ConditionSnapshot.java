package com.mintfrost.weatherstation;

public class ConditionSnapshot {
    String date;
    String tempValue;
    String humValue;
    String pressureValue;

    public ConditionSnapshot(String date, String tempValue, String humValue, String pressureValue) {
        this.date = date;
        this.tempValue = tempValue;
        this.humValue = humValue;
        this.pressureValue = pressureValue;
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

    public String getPressureValue() {
        return pressureValue;
    }
}
