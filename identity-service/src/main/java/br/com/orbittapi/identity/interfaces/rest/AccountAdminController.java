package br.com.orbittapi.identity.interfaces.rest;

import br.com.orbittapi.identity.application.usecase.DeleteAccountUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountAdminController {

    private final DeleteAccountUseCase deleteAccount;

    public AccountAdminController(DeleteAccountUseCase deleteAccount) {
        this.deleteAccount = deleteAccount;
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> delete(@PathVariable UUID accountId) {
        deleteAccount.execute(accountId);
        return ResponseEntity.noContent().build();
    }
}
