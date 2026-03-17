package ua.fpv.entity.validation.authorities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ua.fpv.entity.response.Permission;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthoritiesSerializer extends StdSerializer<Set<String>> {

    private static final Set<String> VALID_PERMISSIONS = Arrays.stream(Permission.values())
            .map(Permission::getPermission)
            .collect(Collectors.toSet());

    public AuthoritiesSerializer() {
        this(null);
    }

    protected AuthoritiesSerializer(Class<Set<String>> t) {
        super(t);
    }

    @Override
    public void serialize(Set<String> authorities, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (!VALID_PERMISSIONS.containsAll(authorities)) {
            throw new IllegalArgumentException("Invalid authorities provided: " + authorities);
        }

        jsonGenerator.writeStartArray();
        for (String authority : authorities) {
            jsonGenerator.writeString(authority);
        }
        jsonGenerator.writeEndArray();
    }
}

