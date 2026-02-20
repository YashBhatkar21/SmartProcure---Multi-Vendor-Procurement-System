package com.smartprocure.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateProcurementRequestRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testDueDateInFuture() {
        CreateProcurementRequestRequest request = new CreateProcurementRequestRequest();
        request.setTitle("Valid Title");
        request.setDescription("Valid Description");
        request.setBudget(new BigDecimal("100.00"));
        // 1 day in the future
        request.setDueDate(Instant.now().plus(1, ChronoUnit.DAYS));

        Set<jakarta.validation.ConstraintViolation<CreateProcurementRequestRequest>> violations = validator
                .validate(request);

        assertTrue(violations.isEmpty(), "Should not have validation errors for future date");
    }

    @Test
    void testDueDateInPast() {
        CreateProcurementRequestRequest request = new CreateProcurementRequestRequest();
        request.setTitle("Valid Title");
        request.setDescription("Valid Description");
        request.setBudget(new BigDecimal("100.00"));
        // 1 day in the past
        request.setDueDate(Instant.now().minus(1, ChronoUnit.DAYS));

        Set<jakarta.validation.ConstraintViolation<CreateProcurementRequestRequest>> violations = validator
                .validate(request);

        assertEquals(1, violations.size(), "Should have exactly one validation error");
        assertEquals("Due date must be in the future", violations.iterator().next().getMessage());
    }
}
