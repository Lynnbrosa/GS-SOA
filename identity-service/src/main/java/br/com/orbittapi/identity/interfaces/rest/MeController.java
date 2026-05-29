package br.com.orbittapi.identity.interfaces.rest;

import br.com.orbittapi.identity.application.dto.AccountProfileResponse;
import br.com.orbittapi.identity.application.dto.UpdateEmailCommand;
import br.com.orbittapi.identity.application.usecase.GetMyProfileUseCase;
import br.com.orbittapi.identity.application.usecase.UpdateAccountEmailUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/me")
public class MeController {

    private final GetMyProfileUseCase getMyProfile;
    private final UpdateAccountEmailUseCase updateEmail;

    public MeController(GetMyProfileUseCase getMyProfile,
                       UpdateAccountEmailUseCase updateEmail) {
        this.getMyProfile = getMyProfile;
        this.updateEmail = updateEmail;
    }

    @GetMapping
    public ResponseEntity<AccountProfileResponse> me(@AuthenticationPrincipal UUID accountId) {
        return ResponseEntity.ok(getMyProfile.execute(accountId));
    }

    @PutMapping
    public ResponseEntity<AccountProfileResponse> updateMyEmail(
            @AuthenticationPrincipal UUID accountId,
            @Valid @RequestBody UpdateEmailCommand command) {
        return ResponseEntity.ok(updateEmail.execute(accountId, command));
    }
}
