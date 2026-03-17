package ua.fpv.entity.validation.fpvmodel;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ua.fpv.entity.model.FpvDrone;

import java.io.IOException;

public class FpvModelSerializer extends StdSerializer<FpvDrone.FpvModel> {

    private static final long serialVersionUID = 1376504304439963619L;

    public FpvModelSerializer() {
        super(FpvDrone.FpvModel.class);
    }

    public FpvModelSerializer(Class<FpvDrone.FpvModel> t) {
        super(t);
    }

    @Override
    public void serialize(FpvDrone.FpvModel fpvModel, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
        generator.writeString(fpvModel.name());
    }
}
