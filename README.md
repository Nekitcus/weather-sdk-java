# Weather SDK for Java (Spring Boot)

## Overview

The **Weather SDK** is a Java-based software development kit that
provides an easy way for developers to integrate weather data retrieval
functionality from [OpenWeather API](https://openweathermap.org/api).\
It supports both **on-demand** and **polling** modes, automatic caching,
and safe handling of multiple clients with unique API keys.

This SDK is designed for reliability, simplicity, and extendability ---
allowing other developers to integrate it into their applications or
services with minimal setup.

------------------------------------------------------------------------

## Features

✅ Fetch current weather for any city via OpenWeather API\
✅ Smart caching for up to 10 cities (data valid for 10 minutes)\
✅ Supports **on-demand** and **polling** modes\
✅ Thread-safe architecture with per-client isolation\
✅ Exception-based error handling with detailed messages\
✅ Prevents duplicate clients for the same API key\
✅ Lightweight and reactive (based on Spring WebClient)\
✅ Extensible: you can replace the adapter layer for other APIs

------------------------------------------------------------------------

## Requirements

- **Java 17+**
- **Maven or Gradle**
- **Spring Boot (for dependencies)**

------------------------------------------------------------------------

## Installation

### 1. Clone the project

``` bash
git clone https://github.com/Nekitcus/weather-sdk-java.git
cd weather-sdk-java
```

### 2. Configure `.env` file

Create a `.env` file in the project root and add your OpenWeather API
key:

``` env
OPENWEATHER_API_KEY=your_api_key_here
```

### 3. Build the SDK

``` bash
mvn clean install
```

------------------------------------------------------------------------

## Architecture Overview

The SDK follows a modular structure:

  -----------------------------------------------------------------------
Layer Responsibility
  --------------------- -------------------------------------------------
**SDK Layer Public API exposed to developers
(`sdk/`)**

**Service Layer Business logic, caching, polling
(`service/`)**

**Adapter Layer External API interaction (OpenWeather)
(`adapter/`)**

**Cache Layer LRU-like weather cache with TTL
(`cache/`)**

**DTO Layer Transfer objects for OpenWeather responses
(`dto/`)**

**Exception Layer Structured exception hierarchy
(`exception/`)**
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## Core Classes

### 1. `WeatherClientFactory`

Factory class responsible for creating and managing SDK clients.

- Prevents multiple instances with the same API key\
- Manages client lifecycle\
- Supports **ON_DEMAND** and **POLLING** modes

#### Example:

``` java
WebClient webClient = WebClient.builder().build();
String openWeatherHost = "api.openweathermap.org";

WeatherClientFactory factory = new WeatherClientFactory(webClient, openWeatherHost);
WeatherClient client = factory.createClient("your_api_key", WeatherClientFactory.Mode.ON_DEMAND);

WeatherDto weather = client.getCurrentWeather("London");
System.out.println(weather);

client.destroy();
```

------------------------------------------------------------------------

### 2. `WeatherClient`

Interface defining the SDK contract for users.

Methods:

``` java
WeatherDto getCurrentWeather(String city);
void destroy();
```

------------------------------------------------------------------------

### 3. `WeatherService`

Handles caching, API calls, and polling.

- Returns cached data if less than 10 minutes old\
- Refreshes data periodically in **POLLING** mode\
- Uses `OpenWeatherAdapter` for HTTP communication

------------------------------------------------------------------------

### 4. `OpenWeatherAdapter`

Interface defining an adapter for the OpenWeather API.\
Implemented by `OpenWeatherAdapterImpl` using Spring's `WebClient`.

------------------------------------------------------------------------

### 5. `WeatherCache`

Custom LRU-like cache for storing up to 10 city results.

Rules: - Data older than 10 minutes is invalidated - Oldest entries are
evicted if capacity exceeded

------------------------------------------------------------------------

### 6. Exception Hierarchy

Exception Description
  ----------------------------- --------------------------------
`WeatherSdkException`         Base SDK exception
`InvalidParameterException`   Invalid or missing parameters
`ExternalApiException`        API call or network error
`CityNotFoundException`       City not found in API response

------------------------------------------------------------------------

## Usage Examples

### Example 1: On-Demand Mode

Fetch weather data only when requested:

``` java
WeatherClient client = factory.createClient(apiKey, WeatherClientFactory.Mode.ON_DEMAND);
WeatherDto weather = client.getCurrentWeather("Berlin");
System.out.println(weather);
client.destroy();
```

### Example 2: Polling Mode

Automatically updates all stored cities in the background:

``` java
WeatherClient client = factory.createClient(apiKey, WeatherClientFactory.Mode.POLLING);
client.getCurrentWeather("New York");
client.getCurrentWeather("Tokyo");
// SDK automatically refreshes cache every 60 seconds
```

### Example 3: Handling Exceptions

``` java
try {
    WeatherDto weather = client.getCurrentWeather("InvalidCityName");
} catch (CityNotFoundException e) {
    System.err.println("City not found: " + e.getMessage());
} catch (ExternalApiException e) {
    System.err.println("API error: " + e.getStatusCode());
}
```

------------------------------------------------------------------------

## Advanced Features

### 1. Client Management

``` java
factory.deleteClient(apiKey); // Destroys and removes client
WeatherClient existing = factory.getClient(apiKey);
```

### 2. Polling Interval Configuration

You can easily adjust `pollIntervalSeconds` or `ttlSeconds` in the
factory setup.

### 3. Extending the SDK

You can implement your own `OpenWeatherAdapter` for other APIs or custom
logic.

------------------------------------------------------------------------

## Logging and Monitoring

The SDK uses **SLF4J** for structured logging: - INFO: Client creation
and lifecycle - WARN: Failed API refresh - ERROR: Unexpected internal
errors

You can easily integrate with Logback or any SLF4J-compatible logging
system.

------------------------------------------------------------------------

## JSON Response Example

``` json
{
  "weather": {
    "main": "Clouds",
    "description": "scattered clouds"
  },
  "temperature": {
    "temp": 269.6,
    "feels_like": 267.57
  },
  "visibility": 10000,
  "wind": {
    "speed": 1.38
  },
  "datetime": 1675744800,
  "sys": {
    "sunrise": 1675751262,
    "sunset": 1675787560
  },
  "timezone": 3600,
  "name": "Zocca"
}
```

