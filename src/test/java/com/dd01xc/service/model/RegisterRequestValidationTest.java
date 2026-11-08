package com.dd01xc.service.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class RegisterRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private RegisterRequest request(String username, String password) {
        RegisterRequest r = new RegisterRequest();
        r.setUsername(username);
        r.setEmail("user@example.com");
        r.setPassword(password);
        return r;
    }

    private Set<String> invalidFields(RegisterRequest r) {
        return validator.validate(r).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void validate_validRequest_noViolations() {
        assertThat(invalidFields(request("john.doe_1", "password123"))).isEmpty();
    }

    @Test
    void validate_usernameWithNewline_rejected() {
        assertThat(invalidFields(request("a\nWARN fake", "password123"))).contains("username");
    }

    @Test
    void validate_usernameWithSpace_rejected() {
        assertThat(invalidFields(request("john doe", "password123"))).contains("username");
    }

    @Test
    void validate_asciiPassword72Bytes_accepted() {
        assertThat(invalidFields(request("john", "a".repeat(72)))).isEmpty();
    }

    @Test
    void validate_asciiPassword73Bytes_rejected() {
        assertThat(invalidFields(request("john", "a".repeat(73)))).contains("passwordWithinBcryptLimit");
    }

    @Test
    void validate_cyrillicPassword40Chars_rejected() {
        //40 chars * 2 bytes = 80 bytes
        assertThat(invalidFields(request("john", "ж".repeat(40)))).contains("passwordWithinBcryptLimit");
    }
}
