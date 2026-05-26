package br.com.orbittapi.satellite.infrastructure.persistence;

import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.SatelliteQuery;
import br.com.orbittapi.satellite.domain.repository.SatelliteQueryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class SatelliteQueryRepositoryImpl implements SatelliteQueryRepository {

    private final SatelliteQueryJpaRepository jpa;

    public SatelliteQueryRepositoryImpl(SatelliteQueryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public SatelliteQuery save(SatelliteQuery query) {
        SatelliteQueryJpaEntity entity = new SatelliteQueryJpaEntity();
        entity.setId(query.id());
        entity.setAccountId(query.accountId());
        entity.setType(query.type());
        entity.setLatitude(query.coordinate().latitude());
        entity.setLongitude(query.coordinate().longitude());
        entity.setCacheHit(query.cacheHit());
        entity.setExecutedAt(query.executedAt());

        SatelliteQueryJpaEntity saved = jpa.save(entity);

        return SatelliteQuery.rehydrate(
                saved.getId(),
                saved.getAccountId(),
                saved.getType(),
                new Coordinate(saved.getLatitude(), saved.getLongitude()),
                saved.isCacheHit(),
                saved.getExecutedAt()
        );
    }
}
