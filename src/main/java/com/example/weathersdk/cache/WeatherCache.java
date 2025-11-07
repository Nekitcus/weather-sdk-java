package com.example.weathersdk.cache;

import com.example.weathersdk.dto.WeatherDto;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class WeatherCache {

    private final int capacity;

    private final long ttlSeconds;

    private final LinkedHashMap<String, Entry> map;

    public WeatherCache(int capacity, long ttlSeconds) {
        this.capacity = capacity;
        this.ttlSeconds = ttlSeconds;
        this.map = new LinkedHashMap<>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
                return size() > WeatherCache.this.capacity;
            }
        };
    }

    public synchronized void put(String cityKey, WeatherDto dto) {
        map.put(cityKey, new Entry(dto, Instant.now().getEpochSecond()));
    }

    public synchronized Optional<WeatherDto> getIfFresh(String cityKey) {
        Entry e = map.get(cityKey);
        if (e == null) return Optional.empty();
        long now = Instant.now().getEpochSecond();
        if (now - e.storedAt <= ttlSeconds) {
            return Optional.of(e.dto);
        } else {
            map.remove(cityKey);
            return Optional.empty();
        }
    }

    public synchronized void remove(String cityKey) {
        map.remove(cityKey);
    }

    public synchronized Set<String> keys() {
        return Set.copyOf(map.keySet());
    }

    public synchronized void clear() {
        map.clear();
    }

    private static class Entry {

        final WeatherDto dto;

        final long storedAt;

        Entry(WeatherDto dto, long storedAt) {
            this.dto = dto;
            this.storedAt = storedAt;
        }
    }
}
