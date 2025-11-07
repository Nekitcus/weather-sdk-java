package com.example.weathersdk.sdk;

import com.example.weathersdk.dto.WeatherDto;
import com.example.weathersdk.exception.WeatherSdkException;

public interface WeatherClient {

    WeatherDto getCurrentWeather(String city) throws WeatherSdkException;

    void destroy();
}
