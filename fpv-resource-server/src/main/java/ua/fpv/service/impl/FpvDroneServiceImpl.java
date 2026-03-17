package ua.fpv.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.fpv.entity.model.FpvDrone;
import ua.fpv.entity.request.FpvDroneRequest;
import ua.fpv.entity.response.FpvDroneResponse;

@Slf4j
@Service
public class FpvDroneServiceImpl implements FpvDroneService {

    public FpvDrone mapToFpvDroneEntity(FpvDroneRequest request) {
        return FpvDrone.builder()
                .fpvSerialNumber(request.getFpvSerialNumber())
                .fpvCraftName(request.getFpvCraftName())
                .fpvModel(request.getFpvModel())
                .build();
    }

    public FpvDroneResponse mapToFPVDroneResponse(FpvDrone fpvDrone) {
        return FpvDroneResponse.builder()
                .fpvSerialNumber(fpvDrone.getFpvSerialNumber())
                .fpvCraftName(fpvDrone.getFpvCraftName())
                .fpvModel(fpvDrone.getFpvModel())
                .build();
    }
}
