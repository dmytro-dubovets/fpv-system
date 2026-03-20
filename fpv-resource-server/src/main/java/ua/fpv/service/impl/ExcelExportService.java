package ua.fpv.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ua.fpv.entity.model.FpvReport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    public byte[] exportReportsToExcel(List<FpvReport> reports) throws IOException {
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
            for (FpvReport report : reports) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(report.getFpvReportId());
                row.createCell(1).setCellValue(report.getFpvDrone().getFpvSerialNumber());
                row.createCell(2).setCellValue(report.getFpvDrone().getFpvModel().toString());
                row.createCell(3).setCellValue(report.isOnTargetFPV() ? "Влучання" : "Промах/Обрив");
                row.createCell(4).setCellValue(report.getCoordinatesMGRS());
                row.createCell(5).setCellValue(report.getAdditionalInfo());
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
