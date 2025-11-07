package com.example.weathersdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherMapDto {

    public List<Weather> weather;

    public Main main;

    public Integer visibility;

    public Wind wind;

    public Long dt;

    public Sys sys;

    public Integer timezone;

    public String name;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {

        public String main;

        public String description;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {

        public Double temp;

        @JsonProperty("feels_like")
        public Double feelsLike;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wind {

        public Double speed;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sys {

        public Long sunrise;

        public Long sunset;
    }
}
