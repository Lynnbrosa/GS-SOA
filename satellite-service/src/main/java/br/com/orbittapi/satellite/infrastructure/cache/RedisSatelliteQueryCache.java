package br.com.orbittapi.satellite.infrastructure.cache;

import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.LandUseSnapshot;
import br.com.orbittapi.satellite.domain.model.VegetationSnapshot;
import br.com.orbittapi.satellite.domain.port.SatelliteQueryCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisSatelliteQueryCache implements SatelliteQueryCache {

    private static final Logger log = LoggerFactory.getLogger(RedisSatelliteQueryCache.class);
    private static final Duration TTL = Duration.ofHours(6);

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisSatelliteQueryCache(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public Optional<LandUseSnapshot> getLandUse(Coordinate c) {
        return get(landUseKey(c), CacheableSnapshots.LandUseDto.class)
                .map(CacheableSnapshots.LandUseDto::toDomain);
    }

    @Override
    public void putLandUse(Coordinate c, LandUseSnapshot snapshot) {
        put(landUseKey(c), CacheableSnapshots.LandUseDto.fromDomain(snapshot));
    }

    @Override
    public Optional<VegetationSnapshot> getVegetation(Coordinate c) {
        return get(vegetationKey(c), CacheableSnapshots.VegetationDto.class)
                .map(CacheableSnapshots.VegetationDto::toDomain);
    }

    @Override
    public void putVegetation(Coordinate c, VegetationSnapshot snapshot) {
        put(vegetationKey(c), CacheableSnapshots.VegetationDto.fromDomain(snapshot));
    }

    private <T> Optional<T> get(String key, Class<T> type) {
        try {
            String json = redis.opsForValue().get(key);
            if (json == null) return Optional.empty();
            return Optional.of(mapper.readValue(json, type));
        } catch (Exception ex) {
            log.warn("Cache read failed for key={}: {}", key, ex.getMessage());
            return Optional.empty();
        }
    }

    private void put(String key, Object value) {
        try {
            String json = mapper.writeValueAsString(value);
            redis.opsForValue().set(key, json, TTL);
        } catch (JsonProcessingException ex) {
            log.warn("Cache write failed for key={}: {}", key, ex.getMessage());
        }
    }

    private static String landUseKey(Coordinate c) {
        return "landuse:" + c.latitude() + ":" + c.longitude();
    }

    private static String vegetationKey(Coordinate c) {
        return "vegetation:" + c.latitude() + ":" + c.longitude();
    }
}
