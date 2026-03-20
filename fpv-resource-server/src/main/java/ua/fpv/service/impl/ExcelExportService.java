package ua.fpv.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ua.fpv.entity.response.FpvReportResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    public byte[] exportReportsToExcel(List<FpvReportResponse> reports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reports");

            // Заголовок
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Серійник");
            header.createCell(2).setCellValue("Модель");
            header.createCell(3).setCellValue("Результат");
            header.createCell(4).setCellValue("Координати");
            header.createCell(5).setCellValue("Додатково");

            // Дані
            int rowIdx = 1;
            for (FpvReportResponse report : reports) {
                Row row = sheet.createRow(rowIdx++);

                // Використовуємо геттери саме твого FpvReportResponse
                row.createCell(0).setCellValue(report.getFpvReportId() != null ? report.getFpvReportId() : 0);

                // Перевір назви геттерів у своєму DTO (можливо просто getFpvSerialNumber())
                // Серійник
                if (report.getFpvDrone() != null && report.getFpvDrone().getFpvSerialNumber() != null) {
                    row.createCell(1).setCellValue(report.getFpvDrone().getFpvSerialNumber());
                } else {
                    row.createCell(1).setCellValue("Н/Д");
                }

                if (report.getFpvDrone() != null && report.getFpvDrone().getFpvModel() != null) {
                    row.createCell(2).setCellValue(report.getFpvDrone().getFpvModel().toString());
                } else {
                    row.createCell(2).setCellValue("-");
                }
                // Обробка результату (влучання/промах/обрив)
                row.createCell(3).setCellValue(report.isOnTargetFPV() ? "Влучання" : "Промах/Обрив");

                row.createCell(4).setCellValue(report.getCoordinatesMGRS());
                row.createCell(5).setCellValue(report.getAdditionalInfo());
            }

            // Автоматичне підлаштування ширини колонок (опціонально, але зручно)
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
