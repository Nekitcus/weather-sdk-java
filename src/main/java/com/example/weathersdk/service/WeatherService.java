package com.example.weathersdk.service;

import com.example.weathersdk.adapter.OpenWeatherAdapter;
import com.example.weathersdk.cache.WeatherCache;
import com.example.weathersdk.dto.OpenWeatherMapDto;
import com.example.weathersdk.dto.WeatherDto;
import com.example.weathersdk.exception.CityNotFoundException;
import com.example.weathersdk.exception.ExternalApiException;
import com.example.weathersdk.exception.InvalidParameterException;
import com.example.weathersdk.exception.WeatherSdkException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final OpenWeatherAdapter adapter;

    private final WeatherCache cache;

    private final boolean polling;

    private final long pollIntervalSeconds;

    private ScheduledExecutorService scheduler;

    private String apiKey; // добавили хранение apiKey

    public void init(String apiKey) {
        this.apiKey = apiKey;
        if (polling) startPolling();
    }

    public void startPolling() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::refreshAll, 0, pollIntervalSeconds, TimeUnit.SECONDS);
        log.info("Polling started with interval {}s", pollIntervalSeconds);
    }

    public void stopPolling() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            log.info("Polling stopped");
        }
    }

    public WeatherDto getCurrentWeather(String apiKey, String city) {
        if (apiKey == null || apiKey.isBlank()) throw new InvalidParameterException("apiKey is required");
        if (city == null || city.isBlank()) throw new InvalidParameterException("city is required");

        String key = normalize(city);

        var cached = cache.getIfFresh(key);
        if (cached.isPresent()) {
            log.debug("Cache hit for city '{}'", city);
            return cached.get();
        }

        log.debug("Cache miss for city '{}', calling API...", city);
        try {
            OpenWeatherMapDto raw = adapter.getWeatherByCity(apiKey, city);
            if (raw == null || raw.name == null) throw new CityNotFoundException(city);
            WeatherDto dto = map(raw);
            cache.put(key, dto);
            return dto;
        } catch (WeatherSdkException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalApiException("Unexpected error while fetching weather: " + e.getMessage(), 500, e);
        }
    }

    private void refreshAll() {
        if (apiKey == null) {
            log.warn("Skipping refreshAll — apiKey not initialized");
            return;
        }

        try {
            Set<String> keys = cache.keys();
            for (String cityKey : keys) {
                try {
                    OpenWeatherMapDto raw = adapter.getWeatherByCity(apiKey, cityKey);
                    if (raw != null && raw.name != null) {
                        cache.put(cityKey, map(raw));
                        log.debug("Refreshed weather for {}", cityKey);
                    }
                } catch (ExternalApiException e) {
                    log.warn("Failed to refresh city {}: {}", cityKey, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in refreshAll: {}", e.getMessage(), e);
        }
    }

    private WeatherDto map(OpenWeatherMapDto raw) {
        var weatherBlock = raw.weather != null && !raw.weather.isEmpty()
                ? new WeatherDto.WeatherBlock(raw.weather.get(0).main, raw.weather.get(0).description)
                : new WeatherDto.WeatherBlock(null, null);
        var tempBlock = raw.main != null
                ? new WeatherDto.TemperatureBlock(raw.main.temp, raw.main.feelsLike)
                : new WeatherDto.TemperatureBlock(null, null);
        var windBlock = raw.wind != null ? new WeatherDto.WindBlock(raw.wind.speed) : new WeatherDto.WindBlock(null);
        var sysBlock = raw.sys != null
                ? new WeatherDto.SysBlock(raw.sys.sunrise, raw.sys.sunset)
                : new WeatherDto.SysBlock(null, null);

        return new WeatherDto(weatherBlock, tempBlock, raw.visibility, windBlock, raw.dt, sysBlock, raw.timezone, raw.name);
    }

    private String normalize(String city) {
        return city.trim().toLowerCase();
    }

    public void clearCache() {
        cache.clear();
    }
}
