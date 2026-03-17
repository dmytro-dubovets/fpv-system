package ua.fpv.entity.validation.fpvmodel;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;
import ua.fpv.entity.model.FpvDrone;

import java.io.IOException;
import java.util.regex.Pattern;

@Slf4j
public class FpvModelDeserializer extends StdDeserializer<FpvDrone.FpvModel> {

    private static final long serialVersionUID = -1166032307856492833L;

    private static final Pattern ENUM_PATTERN = Pattern.compile("KAMIKAZE|BOMBER|PPO");

    public FpvModelDeserializer() {
        this(null);
    }

    protected FpvModelDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public FpvDrone.FpvModel deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText().toUpperCase();
        if (!ENUM_PATTERN.matcher(value).matches()) {
            throw JsonMappingException.from(p, "Invalid Fpv Model! Must be KAMIKAZE, BOMBER, or PPO");
        }
        return FpvDrone.FpvModel.valueOf(value);
    }


}