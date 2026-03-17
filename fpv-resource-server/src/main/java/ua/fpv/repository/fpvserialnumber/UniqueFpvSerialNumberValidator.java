package ua.fpv.repository.fpvserialnumber;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import ua.fpv.entity.request.FpvReportCreateRequest;
import ua.fpv.repository.FpvReportRepository;

@RequiredArgsConstructor
public class UniqueFpvSerialNumberValidator implements ConstraintValidator<UniqueFpvSerialNumber, FpvReportCreateRequest> {

    private final FpvReportRepository fpvReportRepository;

    @Override
    public boolean isValid(FpvReportCreateRequest fpvReport, ConstraintValidatorContext context) {
        if (fpvReport == null || fpvReport.getFpvDrone() == null || fpvReport.getFpvDrone().getFpvSerialNumber() == null) {
            return true;
        }
        return fpvReportRepository.findAllByFpvDrone_FpvSerialNumber(fpvReport.getFpvDrone().getFpvSerialNumber()).isEmpty();
    }
}
