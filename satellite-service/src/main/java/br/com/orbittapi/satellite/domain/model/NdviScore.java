package br.com.orbittapi.satellite.domain.model;

import br.com.orbittapi.satellite.domain.exception.InvalidNdviScoreException;

import java.util.Objects;

public final class NdviScore {

    private final double value;

    public NdviScore(double value) {
        if (value < -1.0 || value > 1.0) {
            throw new InvalidNdviScoreException("NDVI must be in [-1, 1], got " + value);
        }
        this.value = value;
    }

    public double value() {
        return value;
    }

    public VegetationHealth classify() {
        if (value < 0.1) return VegetationHealth.NONE;
        if (value < 0.3) return VegetationHealth.SPARSE;
        if (value < 0.6) return VegetationHealth.MODERATE;
        return VegetationHealth.DENSE;
    }

    public enum VegetationHealth {
        NONE, SPARSE, MODERATE, DENSE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NdviScore ndviScore)) return false;
        return Double.compare(value, ndviScore.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}
