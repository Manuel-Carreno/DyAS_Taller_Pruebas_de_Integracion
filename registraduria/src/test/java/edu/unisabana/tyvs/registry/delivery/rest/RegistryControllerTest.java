package edu.unisabana.tyvs.registry.delivery.rest;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.application.usecase.Registry;
import edu.unisabana.tyvs.registry.domain.model.Person;
import edu.unisabana.tyvs.registry.domain.model.rq.PersonDTO;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class RegistryControllerTest {

    private RegistryRepositoryPort repository;
    private RegistryController controller;

    @Before
    public void setUp() {
        repository = mock(RegistryRepositoryPort.class);
        Registry registry = new Registry(repository);
        controller = new RegistryController(registry);
    }

    @Test
    public void shouldReturn200WhenRegistryReturnsValid() throws Exception {
        // Arrange
        PersonDTO dto = new PersonDTO("Ana", 10, 30, "FEMALE", true);
        when(repository.existsById(10)).thenReturn(false);

        // Act
        ResponseEntity<String> response = controller.register(dto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("VALID", response.getBody());
    }

    @Test
    public void shouldReturn422WhenGenderIsInvalid() {
        // Arrange
        PersonDTO dto = new PersonDTO("Ana", 10, 30, "OTHER", true);

        // Act
        ResponseEntity<String> response = controller.register(dto);

        // Assert
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("INVALID_INPUT", response.getBody());
    }

    @Test
    public void shouldReturn500WhenRegistryThrowsIllegalState() throws Exception {
        // Arrange
        PersonDTO dto = new PersonDTO("Ana", 10, 30, "FEMALE", true);
        doThrow(new RuntimeException("db down")).when(repository).existsById(10);

        // Act
        ResponseEntity<String> response = controller.register(dto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("PERSISTENCE_ERROR"));
    }
}
