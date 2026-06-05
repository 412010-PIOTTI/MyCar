package ar.edu.utn.frc.mycar.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

/** Returns {@code true} when the value is {@code null} or ≤ the current calendar year. */
public class MaxCurrentYearValidator implements ConstraintValidator<MaxCurrentYear, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) return true; // @NotNull handles the null case
        return value <= Year.now().getValue();
    }
}
