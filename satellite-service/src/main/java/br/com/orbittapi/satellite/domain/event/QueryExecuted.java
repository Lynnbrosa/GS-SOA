package br.com.orbittapi.satellite.domain.event;

import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.QueryType;

import java.time.Instant;
import java.util.UUID;

public record QueryExecuted(
        UUID queryId,
        UUID accountId,
        QueryType type,
        Coordinate coordinate,
        boolean cacheHit,
        Instant occurredOn
) implements DomainEvent {

    public static QueryExecuted now(UUID queryId, UUID accountId, QueryType type,
                                    Coordinate coordinate, boolean cacheHit) {
        return new QueryExecuted(queryId, accountId, type, coordinate, cacheHit, Instant.now());
    }
}
