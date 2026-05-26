package br.com.orbittapi.satellite.application.port;

import br.com.orbittapi.satellite.domain.event.DomainEvent;

import java.util.List;

public interface DomainEventPublisher {
    void publishAll(List<? extends DomainEvent> events);
}
