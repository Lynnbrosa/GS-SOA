package br.com.orbittapi.satellite.infrastructure.cache;

import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.LandUseDistribution;
import br.com.orbittapi.satellite.domain.model.LandUseSnapshot;
import br.com.orbittapi.satellite.domain.model.NdviScore;
import br.com.orbittapi.satellite.domain.model.SatelliteSource;
import br.com.orbittapi.satellite.domain.model.VegetationSnapshot;

import java.time.LocalDate;

/**
 * Records Jackson-friendly usados apenas no cache.
 * Mantemos o dominio livre de anotacoes de infraestrutura.
 */
final class CacheableSnapshots {

    private CacheableSnapshots() {
    }

    record LandUseDto(
            double latitude,
            double longitude,
            double vegetationPercent,
            double urbanPercent,
            double waterPercent,
            double bareSoilPercent,
            LocalDate imageDate,
            SatelliteSource source
    ) {
        static LandUseDto fromDomain(LandUseSnapshot snapshot) {
            return new LandUseDto(
                    snapshot.coordinate().latitude(),
                    snapshot.coordinate().longitude(),
                    snapshot.distribution().vegetationPercent(),
                    snapshot.distribution().urbanPercent(),
                    snapshot.distribution().waterPercent(),
                    snapshot.distribution().bareSoilPercent(),
                    snapshot.imageDate(),
                    snapshot.source()
            );
        }

        LandUseSnapshot toDomain() {
            return new LandUseSnapshot(
                    new Coordinate(latitude, longitude),
                    new LandUseDistribution(vegetationPercent, urbanPercent, waterPercent, bareSoilPercent),
                    imageDate,
                    source
            );
        }
    }

    record VegetationDto(
            double latitude,
            double longitude,
            double ndvi,
            LocalDate imageDate,
            SatelliteSource source
    ) {
        static VegetationDto fromDomain(VegetationSnapshot snapshot) {
            return new VegetationDto(
                    snapshot.coordinate().latitude(),
                    snapshot.coordinate().longitude(),
                    snapshot.ndvi().value(),
                    snapshot.imageDate(),
                    snapshot.source()
            );
        }

        VegetationSnapshot toDomain() {
            return new VegetationSnapshot(
                    new Coordinate(latitude, longitude),
                    new NdviScore(ndvi),
                    imageDate,
                    source
            );
        }
    }
}
