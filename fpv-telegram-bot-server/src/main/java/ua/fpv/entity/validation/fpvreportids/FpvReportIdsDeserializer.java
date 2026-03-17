package ua.fpv.entity.validation.fpvreportids;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;

public class FpvReportIdsDeserializer extends StdDeserializer<List<Long>> {

    public FpvReportIdsDeserializer() {
        super(List.class);
    }

    @Override
    public List<Long> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonMappingException {
        List<?> rawIds = p.readValueAs(List.class);

        return rawIds.stream()
                .map(obj -> {
                    if (obj instanceof Number) {
                        return ((Number) obj).longValue();
                    } else if (obj instanceof String) {
                        try {
                            return parseLong((String) obj);
                        } catch (NumberFormatException e) {
                            throw new NumberFormatException("Invalid ID format: '" + obj + "' is not a valid number.");
                        }
                    } else {
                        throw new NumberFormatException("Invalid ID format: '" + obj + "' is not a valid number.");
                    }
                })
                .collect(Collectors.toList());
    }
}
