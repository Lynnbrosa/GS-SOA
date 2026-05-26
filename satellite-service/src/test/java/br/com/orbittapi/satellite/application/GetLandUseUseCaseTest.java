package br.com.orbittapi.satellite.application;

import br.com.orbittapi.satellite.application.dto.LandUseResponse;
import br.com.orbittapi.satellite.application.port.DomainEventPublisher;
import br.com.orbittapi.satellite.application.usecase.GetLandUseUseCase;
import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.LandUseDistribution;
import br.com.orbittapi.satellite.domain.model.LandUseSnapshot;
import br.com.orbittapi.satellite.domain.model.SatelliteQuery;
import br.com.orbittapi.satellite.domain.model.SatelliteSource;
import br.com.orbittapi.satellite.domain.port.SatelliteDataSource;
import br.com.orbittapi.satellite.domain.port.SatelliteQueryCache;
import br.com.orbittapi.satellite.domain.repository.SatelliteQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class GetLandUseUseCaseTest {

    private SatelliteDataSource dataSource;
    private SatelliteQueryCache cache;
    private SatelliteQueryRepository queryRepository;
    private DomainEventPublisher eventPublisher;
    private GetLandUseUseCase useCase;

    @BeforeEach
    void setUp() {
        dataSource = mock(SatelliteDataSource.class);
        cache = mock(SatelliteQueryCache.class);
        queryRepository = mock(SatelliteQueryRepository.class);
        eventPublisher = mock(DomainEventPublisher.class);
        useCase = new GetLandUseUseCase(dataSource, cache, queryRepository, eventPublisher);
    }

    @Test
    void fetchesFromDataSourceWhenCacheMiss() {
        UUID accountId = UUID.randomUUID();
        Coordinate coord = new Coordinate(-23.5, -46.6);
        LandUseSnapshot snapshot = new LandUseSnapshot(
                coord,
                new LandUseDistribution(50, 20, 10, 20),
                LocalDate.now(),
                SatelliteSource.MOCK
        );

        when(cache.getLandUse(coord)).thenReturn(Optional.empty());
        when(dataSource.fetchLandUse(coord)).thenReturn(snapshot);
        when(queryRepository.save(any(SatelliteQuery.class))).thenAnswer(inv -> inv.getArgument(0));

        LandUseResponse response = useCase.execute(accountId, -23.5, -46.6);

        assertThat(response.cacheHit()).isFalse();
        assertThat(response.vegetationPercent()).isEqualTo(50);
        verify(cache).putLandUse(coord, snapshot);
        verify(queryRepository).save(any(SatelliteQuery.class));
        verify(eventPublisher, times(1)).publishAll(any());
    }

    @Test
    void servesFromCacheWhenHit() {
        UUID accountId = UUID.randomUUID();
        Coordinate coord = new Coordinate(-23.5, -46.6);
        LandUseSnapshot snapshot = new LandUseSnapshot(
                coord,
                new LandUseDistribution(40, 30, 20, 10),
                LocalDate.now(),
                SatelliteSource.MOCK
        );

        when(cache.getLandUse(coord)).thenReturn(Optional.of(snapshot));
        when(queryRepository.save(any(SatelliteQuery.class))).thenAnswer(inv -> inv.getArgument(0));

        LandUseResponse response = useCase.execute(accountId, -23.5, -46.6);

        assertThat(response.cacheHit()).isTrue();
        verify(dataSource, never()).fetchLandUse(any());
        verify(cache, never()).putLandUse(any(), any());
    }
}
