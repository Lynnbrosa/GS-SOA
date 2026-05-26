package br.com.orbittapi.satellite.application.usecase;

import br.com.orbittapi.satellite.application.dto.LandUseResponse;
import br.com.orbittapi.satellite.application.port.DomainEventPublisher;
import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.LandUseSnapshot;
import br.com.orbittapi.satellite.domain.model.QueryType;
import br.com.orbittapi.satellite.domain.model.SatelliteQuery;
import br.com.orbittapi.satellite.domain.port.SatelliteDataSource;
import br.com.orbittapi.satellite.domain.port.SatelliteQueryCache;
import br.com.orbittapi.satellite.domain.repository.SatelliteQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetLandUseUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetLandUseUseCase.class);

    private final SatelliteDataSource dataSource;
    private final SatelliteQueryCache cache;
    private final SatelliteQueryRepository queryRepository;
    private final DomainEventPublisher eventPublisher;

    public GetLandUseUseCase(SatelliteDataSource dataSource,
                             SatelliteQueryCache cache,
                             SatelliteQueryRepository queryRepository,
                             DomainEventPublisher eventPublisher) {
        this.dataSource = dataSource;
        this.cache = cache;
        this.queryRepository = queryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public LandUseResponse execute(UUID accountId, double lat, double lng) {
        Coordinate coordinate = new Coordinate(lat, lng);

        Optional<LandUseSnapshot> cached = cache.getLandUse(coordinate);
        boolean cacheHit = cached.isPresent();
        LandUseSnapshot snapshot;
        if (cacheHit) {
            snapshot = cached.get();
            log.info("Cache HIT landuse coord={}", coordinate);
        } else {
            snapshot = dataSource.fetchLandUse(coordinate);
            cache.putLandUse(coordinate, snapshot);
            log.info("Cache MISS landuse coord={} fetched from {}", coordinate, snapshot.source());
        }

        SatelliteQuery query = SatelliteQuery.execute(accountId, QueryType.LAND_USE, coordinate, cacheHit);
        SatelliteQuery saved = queryRepository.save(query);
        eventPublisher.publishAll(saved.pullDomainEvents());

        return new LandUseResponse(
                coordinate.latitude(),
                coordinate.longitude(),
                snapshot.distribution().vegetationPercent(),
                snapshot.distribution().urbanPercent(),
                snapshot.distribution().waterPercent(),
                snapshot.distribution().bareSoilPercent(),
                snapshot.imageDate(),
                snapshot.source().name(),
                cacheHit
        );
    }
}
