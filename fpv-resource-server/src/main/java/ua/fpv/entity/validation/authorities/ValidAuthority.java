package ua.fpv.entity.validation.authorities;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AuthoritiesValidator.class)
public @interface ValidAuthority {

    String message() default "Invalid authorities provided!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
