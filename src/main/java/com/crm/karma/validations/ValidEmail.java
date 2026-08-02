package com.crm.karma.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * Ensures a request field is a present, well-formed e-mail within the DB column length.
 */
@Documented
@NotBlank
@Email
@Size(max = 255)
@ReportAsSingleViolation
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface ValidEmail {
  String message() default "must be a well-formed email address";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
