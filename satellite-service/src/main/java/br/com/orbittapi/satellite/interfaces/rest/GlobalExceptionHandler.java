package br.com.orbittapi.satellite.interfaces.rest;

import br.com.orbittapi.satellite.domain.exception.InvalidCoordinateException;
import br.com.orbittapi.satellite.domain.exception.InvalidLandUseDistributionException;
import br.com.orbittapi.satellite.domain.exception.InvalidNdviScoreException;
import br.com.orbittapi.satellite.domain.exception.SatelliteDataUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_TYPE = "https://orbittapi.dev/errors/";

    @ExceptionHandler(InvalidCoordinateException.class)
    public ProblemDetail handleCoordinate(InvalidCoordinateException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "invalid-coordinate", "Invalid coordinate", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidNdviScoreException.class)
    public ProblemDetail handleNdvi(InvalidNdviScoreException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "invalid-ndvi", "Invalid NDVI score", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidLandUseDistributionException.class)
    public ProblemDetail handleLandUse(InvalidLandUseDistributionException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "invalid-land-use", "Invalid land use distribution", ex.getMessage(), req);
    }

    @ExceptionHandler(SatelliteDataUnavailableException.class)
    public ProblemDetail handleUnavailable(SatelliteDataUnavailableException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "satellite-data-unavailable",
                "Satellite data unavailable", ex.getMessage(), req);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        if ("X-User-Id".equalsIgnoreCase(ex.getHeaderName())) {
            return build(HttpStatus.UNAUTHORIZED, "unauthorized", "Unauthorized",
                    "Missing authentication header", req);
        }
        return build(HttpStatus.BAD_REQUEST, "missing-header", "Missing request header", ex.getMessage(), req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "missing-parameter", "Missing parameter",
                "Required parameter '" + ex.getParameterName() + "' is missing", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "type-mismatch", "Parameter type mismatch",
                "Parameter '" + ex.getName() + "' has invalid value", req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "validation-failed", "Validation failed", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleBodyValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "validation-failed", "Validation failed", details, req);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal", "Internal error",
                "Unexpected error processing the request", req);
    }

    private ProblemDetail build(HttpStatus status, String slug, String title, String detail, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create(BASE_TYPE + slug));
        pd.setTitle(title);
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }
}
