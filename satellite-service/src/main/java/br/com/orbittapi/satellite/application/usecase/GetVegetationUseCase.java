package br.com.orbittapi.satellite.application.usecase;

import br.com.orbittapi.satellite.application.dto.VegetationResponse;
import br.com.orbittapi.satellite.application.port.DomainEventPublisher;
import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.QueryType;
import br.com.orbittapi.satellite.domain.model.SatelliteQuery;
import br.com.orbittapi.satellite.domain.model.VegetationSnapshot;
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
public class GetVegetationUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetVegetationUseCase.class);

    private final SatelliteDataSource dataSource;
    private final SatelliteQueryCache cache;
    private final SatelliteQueryRepository queryRepository;
    private final DomainEventPublisher eventPublisher;

    public GetVegetationUseCase(SatelliteDataSource dataSource,
                                SatelliteQueryCache cache,
                                SatelliteQueryRepository queryRepository,
                                DomainEventPublisher eventPublisher) {
        this.dataSource = dataSource;
        this.cache = cache;
        this.queryRepository = queryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public VegetationResponse execute(UUID accountId, double lat, double lng) {
        Coordinate coordinate = new Coordinate(lat, lng);

        Optional<VegetationSnapshot> cached = cache.getVegetation(coordinate);
        boolean cacheHit = cached.isPresent();
        VegetationSnapshot snapshot;
        if (cacheHit) {
            snapshot = cached.get();
            log.info("Cache HIT vegetation coord={}", coordinate);
        } else {
            snapshot = dataSource.fetchVegetation(coordinate);
            cache.putVegetation(coordinate, snapshot);
            log.info("Cache MISS vegetation coord={} fetched from {}", coordinate, snapshot.source());
        }

        SatelliteQuery query = SatelliteQuery.execute(accountId, QueryType.VEGETATION, coordinate, cacheHit);
        SatelliteQuery saved = queryRepository.save(query);
        eventPublisher.publishAll(saved.pullDomainEvents());

        return new VegetationResponse(
                coordinate.latitude(),
                coordinate.longitude(),
                snapshot.ndvi().value(),
                snapshot.ndvi().classify().name(),
                snapshot.imageDate(),
                snapshot.source().name(),
                cacheHit
        );
    }
}
