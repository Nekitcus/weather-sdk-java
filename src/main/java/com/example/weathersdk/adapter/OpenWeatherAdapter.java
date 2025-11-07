package com.example.weathersdk.adapter;

import com.example.weathersdk.dto.OpenWeatherMapDto;
import com.example.weathersdk.exception.ExternalApiException;

public interface OpenWeatherAdapter {

    OpenWeatherMapDto getWeatherByCity(String apiKey, String city) throws ExternalApiException;
}
