package ua.fpv.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ua.fpv.entity.model.PilotRegistry;
import ua.fpv.entity.response.FpvReportResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    public byte[] exportReportsToExcel(List<FpvReportResponse> reports) throws IOException {

        private static final java.time.format.DateTimeFormatter DATE_FORMATTER =
                java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reports");

            // 1. Створюємо заголовок (Індекси 0-7)
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Дата вильоту");
            header.createCell(2).setCellValue("Серійник");
            header.createCell(3).setCellValue("Модель");
            header.createCell(4).setCellValue("Результат");
            header.createCell(5).setCellValue("Координати");
            header.createCell(6).setCellValue("Додатково");
            header.createCell(7).setCellValue("Пілот");

            // 2. Заповнюємо дані
            int rowIdx = 1;
            for (FpvReportResponse report : reports) {
                Row row = sheet.createRow(rowIdx++);

                // ID
                row.createCell(0).setCellValue(report.getFpvReportId() != null ? report.getFpvReportId() : 0);

                // Дата вильоту (використовуємо toString або форматтер)
                row.createCell(1).setCellValue(report.getDateTimeFlight() != null
                        ? report.getDateTimeFlight().format(DATE_FORMATTER)
                        : "Н/Д");

                // Дані про дрон (Серійник та Модель)
                if (report.getFpvDrone() != null) {
                    row.createCell(2).setCellValue(report.getFpvDrone().getFpvSerialNumber());
                    row.createCell(3).setCellValue(report.getFpvDrone().getFpvModel() != null
                            ? report.getFpvDrone().getFpvModel().toString() : "-");
                } else {
                    row.createCell(2).setCellValue("Н/Д");
                    row.createCell(3).setCellValue("-");
                }

                // Результат (isOnTargetFPV)
                row.createCell(4).setCellValue(report.isOnTargetFPV() ? "Влучання" : "Промах/Обрив");

                // Координати
                row.createCell(5).setCellValue(report.getCoordinatesMGRS() != null ? report.getCoordinatesMGRS() : "-");

                // Додатково
                row.createCell(6).setCellValue(report.getAdditionalInfo() != null ? report.getAdditionalInfo() : "-");

                // Пілот (твоє нове поле)
                String pilotDisplayName = "Н/Д";
                if (report.getPilotUsername() != null) {
                    try {
                        Long cid = Long.parseLong(report.getPilotUsername());
                        PilotRegistry pilot = PilotRegistry.getByChatId(cid);
                        pilotDisplayName = pilot.getLastName() + " " + pilot.getFirstName();
                    } catch (NumberFormatException e) {
                        pilotDisplayName = report.getPilotUsername(); // якщо там не число
                    }
                }
                row.createCell(7).setCellValue(pilotDisplayName);
            }

            // 3. Автоматичне підлаштування ширини для всіх 8 колонок
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
