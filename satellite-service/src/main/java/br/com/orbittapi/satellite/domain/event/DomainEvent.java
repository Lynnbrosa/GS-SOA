package br.com.orbittapi.satellite.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}
