package edu.unisabana.tyvs.registry.delivery.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistryControllerIT {

    @TestConfiguration
    static class TestBeans {
        @Bean
        public RegistryRepositoryPort registryRepositoryPort() throws Exception {
            String jdbc = "jdbc:h2:mem:regdb;DB_CLOSE_DELAY=-1";
            var repo = new edu.unisabana.tyvs.registry.infrastructure.persistence.RegistryRepository(jdbc);
            repo.initSchema();
            return repo;
        }

        @Bean
        public edu.unisabana.tyvs.registry.application.usecase.Registry registry(RegistryRepositoryPort port) {
            return new edu.unisabana.tyvs.registry.application.usecase.Registry(port);
        }
    }

    @Autowired
    private TestRestTemplate rest;

    @Test
    public void shouldReturn200AndValidWhenRegisteringEligiblePerson() {
        // Arrange
        String json = "{\"name\":\"Ana\",\"id\":1001,\"age\":30,\"gender\":\"FEMALE\",\"alive\":true}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        // Act
        ResponseEntity<String> resp = rest.postForEntity("/register", request, String.class);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("VALID", resp.getBody());
    }

    @Test
    public void shouldReturn400Or422WhenPayloadIsInvalid() {
        // Arrange: age como string invalido para forzar error de deserializacion
        String invalidJson = "{\"name\":\"Ana\",\"id\":1002,\"age\":\"abc\",\"gender\":\"FEMALE\",\"alive\":true}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(invalidJson, headers);

        // Act
        ResponseEntity<String> resp = rest.postForEntity("/register", request, String.class);

        // Assert
        assertTrue(resp.getStatusCode() == HttpStatus.BAD_REQUEST
                || resp.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
