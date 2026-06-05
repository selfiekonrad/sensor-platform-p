package com.ceniuch.sensordataingestionservice.auth;

import com.ceniuch.common.exceptions.AuthServiceUnavailableException;
import com.ceniuch.common.exceptions.SensorUnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.hamcrest.Matchers.containsString;

class SensorAuthClientTest {

    private MockRestServiceServer server;
    private SensorAuthClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SensorAuthClient("http://localhost:8081", builder);
    }

    @Test
    void validate_managementReturns204_returnsWithoutThrowing() {
        UUID sensorId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        server.expect(requestTo("http://localhost:8081/api/validate"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"sensorId\":\"" + sensorId + "\"")))
                .andExpect(content().string(containsString("\"apiKey\":\"valid-key\"")))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertThatCode(() -> client.validate(sensorId, "valid-key"))
                .doesNotThrowAnyException();

        server.verify();
    }

    @Test
    void validate_managementReturns401_throwsInvalidApiKey() {
        UUID sensorId = UUID.randomUUID();
        server.expect(requestTo("http://localhost:8081/api/validate"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.validate(sensorId, "bad-key"))
                .isInstanceOf(SensorUnauthorizedException.class)
                .hasMessageContaining("Api Key was invalid");
    }

    @Test
    void validate_managementReturns500_throwsAuthServiceUnavailable() {
        UUID sensorId = UUID.randomUUID();
        server.expect(requestTo("http://localhost:8081/api/validate"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.validate(sensorId, "any-key"))
                .isInstanceOf(AuthServiceUnavailableException.class)
                .hasMessageContaining("unexpected status");
    }

    @Test
    void validate_managementUnreachable_throwsAuthServiceUnavailable() {
        UUID sensorId = UUID.randomUUID();
        server.expect(requestTo("http://localhost:8081/api/validate"))
                .andRespond(withException(new IOException("connection refused")));

        assertThatThrownBy(() -> client.validate(sensorId, "any-key"))
                .isInstanceOf(AuthServiceUnavailableException.class)
                .hasMessageContaining("unreachable");
    }
}
