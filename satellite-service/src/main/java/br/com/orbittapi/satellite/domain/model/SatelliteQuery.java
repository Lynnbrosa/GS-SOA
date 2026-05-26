package br.com.orbittapi.satellite.domain.model;

import br.com.orbittapi.satellite.domain.event.DomainEvent;
import br.com.orbittapi.satellite.domain.event.QueryExecuted;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SatelliteQuery {

    private final UUID id;
    private final UUID accountId;
    private final QueryType type;
    private final Coordinate coordinate;
    private final boolean cacheHit;
    private final Instant executedAt;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private SatelliteQuery(UUID id, UUID accountId, QueryType type,
                           Coordinate coordinate, boolean cacheHit, Instant executedAt) {
        this.id = Objects.requireNonNull(id);
        this.accountId = Objects.requireNonNull(accountId);
        this.type = Objects.requireNonNull(type);
        this.coordinate = Objects.requireNonNull(coordinate);
        this.cacheHit = cacheHit;
        this.executedAt = Objects.requireNonNull(executedAt);
    }

    public static SatelliteQuery execute(UUID accountId, QueryType type,
                                         Coordinate coordinate, boolean cacheHit) {
        SatelliteQuery query = new SatelliteQuery(
                UUID.randomUUID(),
                accountId,
                type,
                coordinate,
                cacheHit,
                Instant.now()
        );
        query.domainEvents.add(QueryExecuted.now(query.id, accountId, type, coordinate, cacheHit));
        return query;
    }

    public static SatelliteQuery rehydrate(UUID id, UUID accountId, QueryType type,
                                           Coordinate coordinate, boolean cacheHit, Instant executedAt) {
        return new SatelliteQuery(id, accountId, type, coordinate, cacheHit, executedAt);
    }

    public UUID id() { return id; }
    public UUID accountId() { return accountId; }
    public QueryType type() { return type; }
    public Coordinate coordinate() { return coordinate; }
    public boolean cacheHit() { return cacheHit; }
    public Instant executedAt() { return executedAt; }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    public List<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SatelliteQuery that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
