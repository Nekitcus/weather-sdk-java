package com.example.weathersdk.sdk;

import com.example.weathersdk.adapter.OpenWeatherAdapter;
import com.example.weathersdk.adapter.impl.OpenWeatherAdapterImpl;
import com.example.weathersdk.cache.WeatherCache;
import com.example.weathersdk.dto.WeatherDto;
import com.example.weathersdk.exception.InvalidParameterException;
import com.example.weathersdk.exception.WeatherSdkException;
import com.example.weathersdk.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class WeatherClientFactory {

    private static final Logger log = LoggerFactory.getLogger(WeatherClientFactory.class);

    private final Map<String, WeatherClient> clients = new ConcurrentHashMap<>();

    private final WebClient webClient;

    private final String openWeatherHost;

    private final long ttlSeconds = 600;

    private final int cacheSize = 10;

    private final int maxRetries = 2;

    private final Duration requestTimeout = Duration.ofSeconds(5);

    private final Duration retryBackoff = Duration.ofSeconds(1);

    private final long pollIntervalSeconds = 60;

    public WeatherClient createClient(String apiKey, Mode mode) {
        if (apiKey == null || apiKey.isBlank()) throw new InvalidParameterException("apiKey required");
        Objects.requireNonNull(mode, "mode required");

        if (clients.containsKey(apiKey)) throw new WeatherSdkException("Client with this apiKey already exists");

        synchronized (clients) {
            if (clients.containsKey(apiKey)) throw new WeatherSdkException("Client with this apiKey already exists");

            OpenWeatherAdapter adapter = new OpenWeatherAdapterImpl(webClient, openWeatherHost, requestTimeout, maxRetries, retryBackoff);
            WeatherCache cache = new WeatherCache(cacheSize, ttlSeconds);
            WeatherService service = new WeatherService(adapter, cache, mode == Mode.POLLING, pollIntervalSeconds);
            service.init(apiKey);

            WeatherClient client = new WeatherClient() {
                @Override
                public WeatherDto getCurrentWeather(String city) {
                    try {
                        return service.getCurrentWeather(apiKey, city);
                    } catch (Exception e) {
                        throw e instanceof WeatherSdkException
                                ? (WeatherSdkException) e
                                : new WeatherSdkException("Failed to get weather: " + e.getMessage(), e);
                    }
                }

                @Override
                public void destroy() {
                    service.stopPolling();
                    service.clearCache();
                    clients.remove(apiKey);
                    log.info("Client destroyed for apiKey {}", apiKey);
                }
            };

            clients.put(apiKey, client);
            log.info("WeatherClient created for apiKey {}", apiKey);
            return client;
        }
    }

    public WeatherClient getClient(String apiKey) {
        return clients.get(apiKey);
    }

    public void deleteClient(String apiKey) {
        WeatherClient client = clients.remove(apiKey);
        if (client != null) client.destroy();
    }

    public enum Mode {ON_DEMAND, POLLING}
}
