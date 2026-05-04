package edu.unisabana.tyvs.registry.infrastructure.persistence;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class RegistryRepositoryTest {

    private RegistryRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = new RegistryRepository("jdbc:h2:mem:repo_test;DB_CLOSE_DELAY=-1");
        repository.initSchema();
        repository.deleteAll();
    }

    @Test
    public void shouldPersistAndFindRecordById() throws Exception {
        // Arrange
        int id = 700;

        // Act
        repository.save(id, "Ana", 31, true);
        Optional<RegistryRecord> found = repository.findById(id);

        // Assert
        assertTrue(repository.existsById(id));
        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
        assertEquals("Ana", found.get().getName());
        assertEquals(31, found.get().getAge());
        assertTrue(found.get().isAlive());
    }

    @Test
    public void shouldReturnFalseWhenIdDoesNotExist() throws Exception {
        // Arrange

        // Act
        boolean exists = repository.existsById(999);

        // Assert
        assertFalse(exists);
        assertFalse(repository.findById(999).isPresent());
    }

    @Test
    public void shouldDeleteAllRecords() throws Exception {
        // Arrange
        repository.save(801, "Uno", 20, true);
        repository.save(802, "Dos", 21, true);

        // Act
        repository.deleteAll();

        // Assert
        assertFalse(repository.existsById(801));
        assertFalse(repository.existsById(802));
    }

    @Test
    public void shouldThrowIllegalStateWhenSavingDuplicatedId() throws Exception {
        // Arrange
        repository.save(900, "Base", 35, true);

        // Act
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> repository.save(900, "Duplicado", 40, true));

        // Assert
        assertTrue(thrown.getMessage().contains("No fue posible guardar"));
    }

    @Test
    public void shouldThrowIllegalStateWhenConnectionIsInvalid() {
        // Arrange
        RegistryRepository badRepository = new RegistryRepository("jdbc:invalid://broken");

        // Act
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> badRepository.existsById(1));

        // Assert
        assertTrue(thrown.getMessage().contains("No fue posible consultar existencia"));
    }
}
