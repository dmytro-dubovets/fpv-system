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

    private static final java.time.format.DateTimeFormatter DATE_FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public byte[] exportReportsToExcel(List<FpvReportResponse> reports) throws IOException {

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reports");

            // 1. Створюємо заголовок (Додаємо індекс 8)
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Дата вильоту");
            header.createCell(2).setCellValue("Серійник");
            header.createCell(3).setCellValue("Модель");
            header.createCell(4).setCellValue("Результат");
            header.createCell(5).setCellValue("РЕБ / Втрата"); // НОВА КОЛОНКА
            header.createCell(6).setCellValue("Координати");
            header.createCell(7).setCellValue("Додатково");
            header.createCell(8).setCellValue("Пілот");

            // 2. Заповнюємо дані
            int rowIdx = 1;
            for (FpvReportResponse report : reports) {
                Row row = sheet.createRow(rowIdx++);

                // ID
                row.createCell(0).setCellValue(report.getFpvReportId() != null ? report.getFpvReportId() : 0);

                // Дата
                row.createCell(1).setCellValue(report.getDateTimeFlight() != null
                        ? report.getDateTimeFlight().format(DATE_FORMATTER)
                        : "Н/Д");

                // Дані про дрон
                if (report.getFpvDrone() != null) {
                    row.createCell(2).setCellValue(report.getFpvDrone().getFpvSerialNumber());
                    row.createCell(3).setCellValue(report.getFpvDrone().getFpvModel() != null
                            ? report.getFpvDrone().getFpvModel().toString() : "-");
                } else {
                    row.createCell(2).setCellValue("Н/Д");
                    row.createCell(3).setCellValue("-");
                }

                // Результат вильоту
                String resultStr = "Невідомо";
                if (report.getFlightResult() != null) {
                    resultStr = switch (report.getFlightResult()) {
                        case HIT -> "Влучання";
                        case MISS -> "Промах";
                        case FIBER_CUT -> "Обрив";
                        default -> "Невідомо";
                    };
                }
                row.createCell(4).setCellValue(resultStr);

                // --- НОВИЙ БЛОК: РЕБ / ВТРАТА ---
                // Перевіряємо поле lostFPVDueToREB з вашого Response об'єкта
                String rebStatus = "-";
                if (report.isLostFPVDueToREB()) {
                    rebStatus = "ТАК (РЕБ)";
                } else if (report.getFlightResult() == ua.fpv.entity.model.FlightResult.MISS) {
                    rebStatus = "Ні";
                }
                row.createCell(5).setCellValue(rebStatus);
                // -------------------------------

                // Координати (індекс змістився на 6)
                row.createCell(6).setCellValue(report.getCoordinatesMGRS() != null ? report.getCoordinatesMGRS() : "-");

                // Додатково (індекс змістився на 7)
                row.createCell(7).setCellValue(report.getAdditionalInfo() != null ? report.getAdditionalInfo() : "-");

                // Пілот (індекс змістився на 8)
                String pilotDisplayName = "Н/Д";
                if (report.getPilotUsername() != null) {
                    try {
                        Long cid = Long.parseLong(report.getPilotUsername());
                        PilotRegistry pilot = PilotRegistry.getByChatId(cid);
                        pilotDisplayName = pilot.getLastName() + " " + pilot.getFirstName();
                    } catch (NumberFormatException e) {
                        pilotDisplayName = report.getPilotUsername();
                    }
                }
                row.createCell(8).setCellValue(pilotDisplayName);
            }

            // 3. Автоматичне підлаштування ширини для всіх 9 колонок (0-8)
            for (int i = 0; i <= 8; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}