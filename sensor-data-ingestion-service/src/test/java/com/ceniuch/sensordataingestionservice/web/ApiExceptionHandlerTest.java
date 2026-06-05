package com.ceniuch.sensordataingestionservice.web;

import com.ceniuch.common.exceptions.AuthServiceUnavailableException;
import com.ceniuch.common.exceptions.SensorUnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void unauthorizedMapsTo401() {
        ProblemDetail pd = handler.handleUnauthorized(SensorUnauthorizedException.invalidApiKey());

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(pd.getTitle()).isEqualTo("Unauthorized");
    }

    @Test
    void authUnavailableMapsTo503WithRetryAfter() {
        ResponseEntity<ProblemDetail> response =
                handler.handleAuthUnavailable(new AuthServiceUnavailableException("down"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("5");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
    }

    @Test
    void unexpectedMapsTo500WithoutLeakingInternals() {
        ProblemDetail pd = handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(pd.getDetail()).doesNotContain("boom");
    }
}
