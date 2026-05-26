package br.com.orbittapi.satellite.infrastructure.adapter;

import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.LandUseDistribution;
import br.com.orbittapi.satellite.domain.model.LandUseSnapshot;
import br.com.orbittapi.satellite.domain.model.NdviScore;
import br.com.orbittapi.satellite.domain.model.SatelliteSource;
import br.com.orbittapi.satellite.domain.model.VegetationSnapshot;
import br.com.orbittapi.satellite.domain.port.SatelliteDataSource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;

/**
 * Adapter ACL: gera dados deterministicos baseados na coordenada.
 * Mesma coordenada -> mesmo resultado (facilita testes e demos).
 */
@Component
public class MockSatelliteDataSource implements SatelliteDataSource {

    @Override
    public LandUseSnapshot fetchLandUse(Coordinate coordinate) {
        Random rng = seededRng(coordinate, "landuse");

        double vegetation = roundTo(20 + rng.nextDouble() * 60, 2);
        double urban = roundTo(rng.nextDouble() * 30, 2);
        double water = roundTo(rng.nextDouble() * 15, 2);
        double bareSoil = roundTo(100 - vegetation - urban - water, 2);
        if (bareSoil < 0) {
            double overflow = -bareSoil;
            vegetation = roundTo(vegetation - overflow, 2);
            bareSoil = 0.0;
        }

        LandUseDistribution distribution = new LandUseDistribution(vegetation, urban, water, bareSoil);
        LocalDate imageDate = LocalDate.now().minusDays(rng.nextInt(30));
        return new LandUseSnapshot(coordinate, distribution, imageDate, SatelliteSource.MOCK);
    }

    @Override
    public VegetationSnapshot fetchVegetation(Coordinate coordinate) {
        Random rng = seededRng(coordinate, "vegetation");
        double ndvi = roundTo(-0.2 + rng.nextDouble() * 1.1, 3);
        if (ndvi > 1.0) ndvi = 1.0;
        if (ndvi < -1.0) ndvi = -1.0;

        LocalDate imageDate = LocalDate.now().minusDays(rng.nextInt(30));
        return new VegetationSnapshot(coordinate, new NdviScore(ndvi), imageDate, SatelliteSource.MOCK);
    }

    private Random seededRng(Coordinate c, String namespace) {
        long seed = 17L;
        seed = 31 * seed + Double.hashCode(c.latitude());
        seed = 31 * seed + Double.hashCode(c.longitude());
        seed = 31 * seed + namespace.hashCode();
        return new Random(seed);
    }

    private static double roundTo(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }
}
