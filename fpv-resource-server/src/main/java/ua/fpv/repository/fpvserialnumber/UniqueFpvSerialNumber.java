package ua.fpv.repository.fpvserialnumber;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueFpvSerialNumberValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueFpvSerialNumber {

    String message() default "FPV Report with this serial number is already exists.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
