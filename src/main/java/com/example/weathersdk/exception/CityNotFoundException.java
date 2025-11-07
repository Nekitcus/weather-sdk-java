package com.example.weathersdk.exception;

public class CityNotFoundException extends WeatherSdkException {

    public CityNotFoundException(String city) {
        super("City not found: " + city);
    }
}
