package com.pilotbroker.shared.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
            caffeine("dashboard",    60),     // MarketService — não remover
            caffeine("stockDetail",  30),
            caffeine("screener",     300),
            caffeine("fundamentals", 3600),
            caffeine("governance",   3600),
            caffeine("search",       300)
        ));
        return manager;
    }

    private CaffeineCache caffeine(String name, long ttlSeconds) {
        return new CaffeineCache(name,
            Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                .build());
    }
}
