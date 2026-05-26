package br.com.orbittapi.identity.infrastructure.security;

import br.com.orbittapi.identity.application.port.DomainEventPublisher;
import br.com.orbittapi.identity.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringDomainEventPublisher.class);

    private final ApplicationEventPublisher publisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publishAll(List<? extends DomainEvent> events) {
        for (DomainEvent event : events) {
            log.debug("Publishing domain event {}", event.getClass().getSimpleName());
            publisher.publishEvent(event);
        }
    }
}
