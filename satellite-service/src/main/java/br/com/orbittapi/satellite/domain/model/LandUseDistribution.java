package br.com.orbittapi.satellite.domain.model;

import br.com.orbittapi.satellite.domain.exception.InvalidLandUseDistributionException;

import java.util.Objects;

public final class LandUseDistribution {

    private static final double TOLERANCE = 0.05;

    private final double vegetationPercent;
    private final double urbanPercent;
    private final double waterPercent;
    private final double bareSoilPercent;

    public LandUseDistribution(double vegetationPercent, double urbanPercent,
                               double waterPercent, double bareSoilPercent) {
        validateRange("vegetation", vegetationPercent);
        validateRange("urban", urbanPercent);
        validateRange("water", waterPercent);
        validateRange("bareSoil", bareSoilPercent);

        double total = vegetationPercent + urbanPercent + waterPercent + bareSoilPercent;
        if (Math.abs(total - 100.0) > TOLERANCE) {
            throw new InvalidLandUseDistributionException(
                    "Land use percentages must sum to 100 (+/- " + TOLERANCE + "), got " + total);
        }

        this.vegetationPercent = vegetationPercent;
        this.urbanPercent = urbanPercent;
        this.waterPercent = waterPercent;
        this.bareSoilPercent = bareSoilPercent;
    }

    private static void validateRange(String name, double value) {
        if (value < 0.0 || value > 100.0) {
            throw new InvalidLandUseDistributionException(
                    name + " percent must be in [0, 100], got " + value);
        }
    }

    public double vegetationPercent() {
        return vegetationPercent;
    }

    public double urbanPercent() {
        return urbanPercent;
    }

    public double waterPercent() {
        return waterPercent;
    }

    public double bareSoilPercent() {
        return bareSoilPercent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LandUseDistribution that)) return false;
        return Double.compare(vegetationPercent, that.vegetationPercent) == 0
                && Double.compare(urbanPercent, that.urbanPercent) == 0
                && Double.compare(waterPercent, that.waterPercent) == 0
                && Double.compare(bareSoilPercent, that.bareSoilPercent) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vegetationPercent, urbanPercent, waterPercent, bareSoilPercent);
    }
}
