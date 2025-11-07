package com.example.weathersdk.adapter.impl;

import com.example.weathersdk.adapter.OpenWeatherAdapter;
import com.example.weathersdk.dto.OpenWeatherMapDto;
import com.example.weathersdk.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@RequiredArgsConstructor
public class OpenWeatherAdapterImpl implements OpenWeatherAdapter {

    private static final Logger log = LoggerFactory.getLogger(OpenWeatherAdapterImpl.class);

    private final WebClient webClient;

    private final String baseUrl;

    private final Duration requestTimeout;

    private final int maxRetries;

    private final Duration retryBackoff;

    @Override
    public OpenWeatherMapDto getWeatherByCity(String apiKey, String city) throws ExternalApiException {
        try {
            Mono<OpenWeatherMapDto> mono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(baseUrl.replace("https://", "").replace("http://", ""))
                            .path("/data/2.5/weather")
                            .queryParam("q", city)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "standard")
                            .build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), resp -> resp.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new ExternalApiException("OpenWeather error: " + body, resp.statusCode().value()))))
                    .bodyToMono(OpenWeatherMapDto.class)
                    .timeout(requestTimeout)
                    .retryWhen(Retry.backoff(maxRetries, retryBackoff).filter(throwable -> {
                        return !(throwable instanceof ExternalApiException);
                    }));

            OpenWeatherMapDto dto = mono.block();
            if (dto == null || dto.name == null) {
                throw new ExternalApiException("Empty response or city not found", 404);
            }
            return dto;
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalApiException("Error from OpenWeather: " + e.getResponseBodyAsString(), e.getRawStatusCode(), e);
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call OpenWeather", e);
            throw new ExternalApiException("Failed to call OpenWeather: " + e.getMessage(), 500, e);
        }
    }
}
