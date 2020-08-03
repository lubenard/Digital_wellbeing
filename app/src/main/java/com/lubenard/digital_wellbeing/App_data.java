package com.lubenard.digital_wellbeing;

public class App_data {
    private String name;
    private int percentage;

    App_data(String appName) {
        name = appName;
        percentage = 0;
    }

    public String getName() {
        return name;
    }

    public int getPercentage(){
        return percentage;
    }
}
