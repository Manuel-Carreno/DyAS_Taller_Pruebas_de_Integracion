package edu.unisabana.tyvs.registry.application.usecase;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.domain.model.Gender;
import edu.unisabana.tyvs.registry.domain.model.Person;
import edu.unisabana.tyvs.registry.domain.model.RegisterResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Clase de prueba unitaria para {@link Registry} utilizando un mock de {@link RegistryRepositoryPort}.
 *
 * <p>Estas pruebas ilustran cómo aislar el caso de uso del repositorio real,
 * aplicando dobles de prueba (Mockito) para simular los escenarios.</p>
 *
 * <p><b>Formato AAA:</b></p>
 * <ul>
 *   <li><b>Arrange</b>: se preparan datos y comportamiento del mock.</li>
 *   <li><b>Act</b>: se ejecuta el método bajo prueba.</li>
 *   <li><b>Assert</b>: se verifican resultados y que no haya interacciones no deseadas.</li>
 * </ul>
 *
 * <p><b>Beneficio:</b> este tipo de prueba es una <i>unitaria pura</i>,
 * sin necesidad de levantar bases de datos ni infraestructura adicional.</p>
 */
public class RegistryWithMockTest {

    /** Mock del puerto de persistencia. */
    private RegistryRepositoryPort repo;

    /** Caso de uso bajo prueba, instanciado con el mock. */
    private Registry registry;

    /**
     * Configura el mock y el caso de uso antes de cada prueba.
     *
     * <p>Se crea un mock de {@link RegistryRepositoryPort} usando Mockito
     * y se inyecta en la instancia de {@link Registry}.</p>
     */
    @Before
    public void setUp() {
        repo = mock(RegistryRepositoryPort.class);
        registry = new Registry(repo);
    }

    @Test
    public void shouldCallSaveWhenExistsByIdReturnsFalse() throws Exception {
        // Arrange
        Person person = new Person("Ana", 7, 25, Gender.FEMALE, true);
        when(repo.existsById(7)).thenReturn(false);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.VALID, result);
        verify(repo).existsById(7);
        verify(repo).save(7, "Ana", 25, true);
    }

    @Test
    public void shouldNotCallSaveWhenExistsByIdReturnsTrue() throws Exception {
        // Arrange
        Person person = new Person("Ana", 7, 25, Gender.FEMALE, true);
        when(repo.existsById(7)).thenReturn(true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.DUPLICATED, result);
        verify(repo).existsById(7);
        verify(repo, never()).save(anyInt(), anyString(), anyInt(), anyBoolean());
    }

    @Test
    public void shouldThrowIllegalStateWhenSaveFails() throws Exception {
        // Arrange
        Person person = new Person("Ana", 7, 25, Gender.FEMALE, true);
        when(repo.existsById(7)).thenReturn(false);
        doThrow(new RuntimeException("DB down")).when(repo).save(7, "Ana", 25, true);

        // Act
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> registry.registerVoter(person));

        // Assert
        assertTrue(thrown.getMessage().contains("Persistencia"));
        verify(repo).existsById(7);
        verify(repo).save(7, "Ana", 25, true);
    }

    @Test
    public void shouldReturnInvalidWhenPersonIsNull() throws Exception {
        // Arrange

        // Act
        RegisterResult result = registry.registerVoter(null);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
        verify(repo, never()).existsById(anyInt());
        verify(repo, never()).save(anyInt(), anyString(), anyInt(), anyBoolean());
    }

    @Test
    public void shouldReturnInvalidWhenIdIsZeroOrNegative() throws Exception {
        // Arrange
        Person person = new Person("Ana", 0, 25, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
        verify(repo, never()).existsById(anyInt());
        verify(repo, never()).save(anyInt(), anyString(), anyInt(), anyBoolean());
    }
}
