package ua.fpv.entity.validation.authorities;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ua.fpv.entity.response.Permission;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthoritiesDeserializer extends StdDeserializer<Set<String>> {

    private static final Set<String> VALID_PERMISSIONS = Arrays.stream(Permission.values())
            .map(Permission::getPermission)
            .collect(Collectors.toSet());

    public AuthoritiesDeserializer() {
        this(null);
    }

    public AuthoritiesDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Set<String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        Set<String> authorities = new HashSet<>(jsonParser.readValueAs(Set.class));

        if (!VALID_PERMISSIONS.containsAll(authorities)) {
            throw new IllegalArgumentException("Invalid authorities provided: " + authorities);
        }

        return authorities;    }
}
