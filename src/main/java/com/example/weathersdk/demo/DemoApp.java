package com.example.weathersdk.demo;

import com.example.weathersdk.WeatherSdkInfo;
import com.example.weathersdk.dto.WeatherDto;
import com.example.weathersdk.sdk.WeatherClient;
import com.example.weathersdk.sdk.WeatherClientFactory;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.web.reactive.function.client.WebClient;

public class DemoApp {

    public static void main(String[] args) {
        WeatherSdkInfo.printInfo();

        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("OPENWEATHER_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Set OPENWEATHER_API_KEY in .env");
            return;
        }

        WebClient webClient = WebClient.builder().build();
        WeatherClientFactory factory = new WeatherClientFactory(webClient, "api.openweathermap.org");
        WeatherClient client = factory.createClient(apiKey, WeatherClientFactory.Mode.POLLING);

        try {
            WeatherDto weather = client.getCurrentWeather("Berlin");
            System.out.println("Weather: " + weather);
        } catch (Exception e) {
            System.err.println("Error fetching weather: " + e.getMessage());
        } finally {
            factory.deleteClient(apiKey);
        }
    }
}
