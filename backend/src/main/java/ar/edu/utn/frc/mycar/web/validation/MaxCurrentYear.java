package ar.edu.utn.frc.mycar.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Validates that the annotated {@code Integer} is not greater than the current calendar year. */
@Documented
@Constraint(validatedBy = MaxCurrentYearValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxCurrentYear {

    String message() default "El año no puede ser posterior al año actual";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
