package br.com.orbittapi.satellite.interfaces.rest;

import br.com.orbittapi.satellite.application.dto.VegetationResponse;
import br.com.orbittapi.satellite.application.usecase.GetVegetationUseCase;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/vegetation")
@Validated
public class VegetationController {

    private final GetVegetationUseCase getVegetation;

    public VegetationController(GetVegetationUseCase getVegetation) {
        this.getVegetation = getVegetation;
    }

    @GetMapping
    public ResponseEntity<VegetationResponse> vegetation(
            @RequestHeader("X-User-Id") UUID accountId,
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lng) {
        return ResponseEntity.ok(getVegetation.execute(accountId, lat, lng));
    }
}
