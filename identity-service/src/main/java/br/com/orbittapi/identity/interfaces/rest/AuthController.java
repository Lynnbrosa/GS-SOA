package br.com.orbittapi.identity.interfaces.rest;

import br.com.orbittapi.identity.application.dto.AuthResponse;
import br.com.orbittapi.identity.application.dto.LoginCommand;
import br.com.orbittapi.identity.application.dto.RegisterAccountCommand;
import br.com.orbittapi.identity.application.usecase.LoginUseCase;
import br.com.orbittapi.identity.application.usecase.RegisterAccountUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterAccountUseCase registerAccount;
    private final LoginUseCase login;

    public AuthController(RegisterAccountUseCase registerAccount, LoginUseCase login) {
        this.registerAccount = registerAccount;
        this.login = login;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterAccountCommand command) {
        AuthResponse response = registerAccount.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginCommand command) {
        return ResponseEntity.ok(login.execute(command));
    }
}
