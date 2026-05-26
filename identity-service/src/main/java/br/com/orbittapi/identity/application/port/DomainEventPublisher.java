package br.com.orbittapi.identity.application.port;

import br.com.orbittapi.identity.domain.event.DomainEvent;

import java.util.List;

public interface DomainEventPublisher {
    void publishAll(List<? extends DomainEvent> events);
}
