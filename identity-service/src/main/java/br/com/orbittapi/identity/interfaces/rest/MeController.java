package br.com.orbittapi.identity.interfaces.rest;

import br.com.orbittapi.identity.application.dto.AccountProfileResponse;
import br.com.orbittapi.identity.application.usecase.GetMyProfileUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/me")
public class MeController {

    private final GetMyProfileUseCase getMyProfile;

    public MeController(GetMyProfileUseCase getMyProfile) {
        this.getMyProfile = getMyProfile;
    }

    @GetMapping
    public ResponseEntity<AccountProfileResponse> me(@AuthenticationPrincipal UUID accountId) {
        return ResponseEntity.ok(getMyProfile.execute(accountId));
    }
}
