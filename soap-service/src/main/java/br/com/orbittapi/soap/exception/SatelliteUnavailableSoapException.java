package br.com.orbittapi.soap.exception;

public class SatelliteUnavailableSoapException extends RuntimeException {
    public SatelliteUnavailableSoapException(String message) {
        super(message);
    }

    public SatelliteUnavailableSoapException(String message, Throwable cause) {
        super(message, cause);
    }
}
