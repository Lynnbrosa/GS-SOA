package br.com.orbittapi.identity.interfaces.rest;

import br.com.orbittapi.identity.application.usecase.RevokeApiKeyUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api-keys")
public class ApiKeyController {

    private final RevokeApiKeyUseCase revokeApiKey;

    public ApiKeyController(RevokeApiKeyUseCase revokeApiKey) {
        this.revokeApiKey = revokeApiKey;
    }

    @PostMapping("/{accountId}/revoke")
    public ResponseEntity<Void> revoke(@PathVariable UUID accountId) {
        revokeApiKey.execute(accountId);
        return ResponseEntity.noContent().build();
    }
}
