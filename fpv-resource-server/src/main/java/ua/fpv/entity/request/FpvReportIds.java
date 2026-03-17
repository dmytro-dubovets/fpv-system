package ua.fpv.entity.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import ua.fpv.entity.validation.fpvreportids.FpvReportIdsDeserializer;

import java.util.List;

@Data
public class FpvReportIds {

    @JsonDeserialize(using = FpvReportIdsDeserializer.class)
    private List<Long> fpvReportIds;
}
