package br.com.orbittapi.satellite.domain;

import br.com.orbittapi.satellite.domain.event.DomainEvent;
import br.com.orbittapi.satellite.domain.event.QueryExecuted;
import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.QueryType;
import br.com.orbittapi.satellite.domain.model.SatelliteQuery;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SatelliteQueryTest {

    @Test
    void executingEmitsQueryExecutedEvent() {
        UUID accountId = UUID.randomUUID();
        Coordinate coord = new Coordinate(-23.5, -46.6);

        SatelliteQuery query = SatelliteQuery.execute(accountId, QueryType.LAND_USE, coord, false);

        assertThat(query.id()).isNotNull();
        assertThat(query.accountId()).isEqualTo(accountId);
        assertThat(query.cacheHit()).isFalse();

        List<DomainEvent> events = query.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(QueryExecuted.class);

        QueryExecuted event = (QueryExecuted) events.get(0);
        assertThat(event.type()).isEqualTo(QueryType.LAND_USE);
        assertThat(event.cacheHit()).isFalse();
    }
}
