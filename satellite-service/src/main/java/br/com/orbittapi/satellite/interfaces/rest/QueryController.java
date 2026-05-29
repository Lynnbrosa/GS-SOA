package br.com.orbittapi.satellite.interfaces.rest;

import br.com.orbittapi.satellite.application.dto.RegisterQueryCommand;
import br.com.orbittapi.satellite.application.dto.RegisterQueryResponse;
import br.com.orbittapi.satellite.application.usecase.RegisterQueryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/queries")
@Validated
public class QueryController {

    private final RegisterQueryUseCase registerQuery;

    public QueryController(RegisterQueryUseCase registerQuery) {
        this.registerQuery = registerQuery;
    }

    @PostMapping
    public ResponseEntity<RegisterQueryResponse> register(
            @RequestHeader("X-User-Id") UUID accountId,
            @Valid @RequestBody RegisterQueryCommand command) {
        RegisterQueryResponse response = registerQuery.execute(accountId, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
