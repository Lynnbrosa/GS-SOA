package br.com.orbittapi.satellite.application.usecase;

import br.com.orbittapi.satellite.application.dto.RegisterQueryCommand;
import br.com.orbittapi.satellite.application.dto.RegisterQueryResponse;
import br.com.orbittapi.satellite.application.port.DomainEventPublisher;
import br.com.orbittapi.satellite.domain.exception.InvalidCoordinateException;
import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.QueryType;
import br.com.orbittapi.satellite.domain.model.SatelliteQuery;
import br.com.orbittapi.satellite.domain.repository.SatelliteQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Persiste um SatelliteQuery sem disparar nova consulta satelital.
 * E utilizado pela camada SOAP (registrarConsulta) e pode ser usado por outros
 * clientes REST que queiram registrar consultas em lote.
 */
@Service
public class RegisterQueryUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterQueryUseCase.class);

    private final SatelliteQueryRepository repository;
    private final DomainEventPublisher eventPublisher;

    public RegisterQueryUseCase(SatelliteQueryRepository repository, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RegisterQueryResponse execute(UUID accountId, RegisterQueryCommand command) {
        QueryType type = parseType(command.type());
        Coordinate coord = new Coordinate(command.latitude(), command.longitude());

        SatelliteQuery query = SatelliteQuery.execute(accountId, type, coord, false);
        SatelliteQuery saved = repository.save(query);
        eventPublisher.publishAll(saved.pullDomainEvents());

        log.info("Registered SatelliteQuery id={} account={} type={} coord={}",
                saved.id(), accountId, type, coord);

        return new RegisterQueryResponse(
                saved.id(),
                saved.type().name(),
                saved.coordinate().latitude(),
                saved.coordinate().longitude(),
                "EXECUTED",
                saved.executedAt()
        );
    }

    private QueryType parseType(String raw) {
        try {
            return QueryType.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            throw new InvalidCoordinateException(
                    "Unsupported query type '" + raw + "'. Expected VEGETATION or LAND_USE.");
        }
    }
}
