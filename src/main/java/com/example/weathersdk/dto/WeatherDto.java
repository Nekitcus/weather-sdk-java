package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherDto {

    private WeatherBlock weather;

    private TemperatureBlock temperature;

    private Integer visibility;

    private WindBlock wind;

    private Long datetime;

    private SysBlock sys;

    private Integer timezone;

    private String name;

    @Data
    @AllArgsConstructor
    public static class WeatherBlock {

        private String main;

        private String description;
    }

    @Data
    @AllArgsConstructor
    public static class TemperatureBlock {

        private Double temp;

        private Double feels_like;
    }

    @Data
    @AllArgsConstructor
    public static class WindBlock {

        private Double speed;
    }

    @Data
    @AllArgsConstructor
    public static class SysBlock {

        private Long sunrise;

        private Long sunset;
    }
}
