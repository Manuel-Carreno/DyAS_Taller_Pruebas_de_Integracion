package edu.unisabana.tyvs.registry.application.usecase;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.domain.model.Person;
import edu.unisabana.tyvs.registry.domain.model.RegisterResult;

public class Registry {

    private static final int MIN_AGE = 18;

    private final RegistryRepositoryPort repository;

    public Registry(RegistryRepositoryPort repository) {
        this.repository = repository;
    }

    /**
     * Constructor vacío para compatibilidad con escenarios donde no se inyecta
     * infraestructura. No se usa en flujo productivo.
     */
    public Registry() {
        this.repository = null;
    }

    public RegisterResult registerVoter(Person person) {
        if (person == null) {
            return RegisterResult.INVALID;
        }

        final int id = person.getId();
        if (id <= 0) {
            return RegisterResult.INVALID;
        }

        if (!person.isAlive()) {
            return RegisterResult.DEAD;
        }

        if (person.getAge() < MIN_AGE) {
            return RegisterResult.UNDERAGE;
        }

        try {
            if (repository == null) {
                throw new IllegalStateException("Repository is not configured");
            }

            if (repository.existsById(id)) {
                return RegisterResult.DUPLICATED;
            }

            repository.save(id, person.getName(), person.getAge(), person.isAlive());
            return RegisterResult.VALID;
        } catch (Exception e) {
            throw new IllegalStateException("Persistencia: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                    e);
        }
    }

}
