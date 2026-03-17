package ua.fpv.entity.validation.localdatetime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class LocalDateTimeCustomDeserializer extends StdDeserializer<LocalDateTime> {

    private static final long serialVersionUID = 7147756900093868795L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    protected LocalDateTimeCustomDeserializer() {
        this(LocalDateTime.class);
    }

    protected LocalDateTimeCustomDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String date = p.getText();
        try {
            return LocalDateTime.parse(date, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidFormatException(p, "Invalid date format! Use yyyy-MM-dd HH:mm", date, LocalDateTime.class);
        }
    }
}
