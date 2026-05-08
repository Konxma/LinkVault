package com.konxma.linkvault.infrastructure;

import com.konxma.linkvault.model.Link;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Сервіс для експорту даних у формати офісних документів (Розділ 11.1 методички).
 */
public class ExportService {

  public void exportLinksToExcel(List<Link> links, File file) throws IOException {
    // Створюємо нову книгу Excel формату .xlsx
    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Мої Закладки");

      // Стиль для заголовків (жирний шрифт)
      CellStyle headerStyle = workbook.createCellStyle();
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerStyle.setFont(headerFont);

      // Створюємо рядок заголовків
      Row headerRow = sheet.createRow(0);
      String[] columns = {"ID", "Назва", "URL-адреса", "Теги"};

      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerStyle);
      }

      // Заповнюємо аркуш даними
      int rowNum = 1;
      for (Link link : links) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(link.getLinkId());
        row.createCell(1).setCellValue(link.getTitle());
        row.createCell(2).setCellValue(link.getUrl());
        row.createCell(3).setCellValue(link.getTagsAsString());
      }

      // Автоматично підганяємо ширину колонок під вміст
      for (int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
      }

      // Записуємо дані у файл
      try (FileOutputStream fileOut = new FileOutputStream(file)) {
        workbook.write(fileOut);
      }
    }
  }
}