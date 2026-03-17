package ua.fpv.entity.validation.authorities;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ua.fpv.entity.response.Permission;

import java.util.Set;

public class AuthoritiesValidator implements ConstraintValidator<ValidAuthority, Set<String>> {

    @Override
    public boolean isValid(Set<String> authorities, ConstraintValidatorContext context) {
        if (authorities == null || authorities.isEmpty()) return false;

        for (String authority : authorities) {
            if (!Permission.isValid(authority)) {
                return false;
            }
        }
        return true;
    }
}
