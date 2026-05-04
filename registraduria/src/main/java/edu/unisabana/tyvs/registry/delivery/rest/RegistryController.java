package edu.unisabana.tyvs.registry.delivery.rest;

import edu.unisabana.tyvs.registry.application.usecase.Registry;
import edu.unisabana.tyvs.registry.domain.model.Gender;
import edu.unisabana.tyvs.registry.domain.model.Person;
import edu.unisabana.tyvs.registry.domain.model.RegisterResult;
import edu.unisabana.tyvs.registry.domain.model.rq.PersonDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class RegistryController {

    private final Registry registry;

    public RegistryController(Registry registry) {
        this.registry = registry;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> register(@RequestBody PersonDTO dto) {
        try {
            Person person = new Person(dto.getName(), dto.getId(), dto.getAge(), parseGender(dto.getGender()),
                    dto.isAlive());
            RegisterResult result = registry.registerVoter(person);
            return ResponseEntity.ok(result.name());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.unprocessableEntity().body("INVALID_INPUT");
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().body("PERSISTENCE_ERROR");
        }
    }

    private Gender parseGender(String rawGender) {
        if (rawGender == null) {
            throw new IllegalArgumentException("Gender is required");
        }
        return Gender.valueOf(rawGender.trim().toUpperCase());
    }
}
