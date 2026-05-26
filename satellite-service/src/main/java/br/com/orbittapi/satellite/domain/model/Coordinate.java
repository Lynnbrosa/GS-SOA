package br.com.orbittapi.satellite.domain.model;

import br.com.orbittapi.satellite.domain.exception.InvalidCoordinateException;

import java.util.Objects;

public final class Coordinate {

    private final double latitude;
    private final double longitude;

    public Coordinate(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new InvalidCoordinateException("Latitude must be between -90 and 90, got " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new InvalidCoordinateException("Longitude must be between -180 and 180, got " + longitude);
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate that)) return false;
        return Double.compare(latitude, that.latitude) == 0
                && Double.compare(longitude, that.longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "(" + latitude + "," + longitude + ")";
    }
}
