package com.kma.itmanagement.service;

import com.kma.itmanagement.model.Asset;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    public ByteArrayInputStream exportAssetsToExcel(List<Asset> assets) throws IOException {
        String[] columns = {"ID", "Asset Name / Model", "Type", "Asset Tag", "Location", "Status"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Assets Inventory");
            sheet.setDisplayGridlines(true);

            // --- STYLES DEFINITION ---
            
            // 1. Title Style
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(IndexedColors.DARK_TEAL.getIndex());

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // 2. Subtitle Style
            Font subtitleFont = workbook.createFont();
            subtitleFont.setItalic(true);
            subtitleFont.setFontHeightInPoints((short) 9);
            subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

            CellStyle subtitleStyle = workbook.createCellStyle();
            subtitleStyle.setFont(subtitleFont);
            subtitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // 3. Header Style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // 4. Data Cell Styles
            CellStyle leftAlignStyle = createBaseDataStyle(workbook);
            leftAlignStyle.setAlignment(HorizontalAlignment.LEFT);

            CellStyle centerAlignStyle = createBaseDataStyle(workbook);
            centerAlignStyle.setAlignment(HorizontalAlignment.CENTER);

            // --- BUILD SHEET CONTENT ---

            // Row 0: Title Banner
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(24);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("KMA IT PORTAL - ASSET INVENTORY REPORT");
            titleCell.setCellStyle(titleStyle);

            // Merge Row 0 across Columns A to F (0 to 5)
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // Row 1: Date Subtitle
            Row subtitleRow = sheet.createRow(1);
            subtitleRow.setHeightInPoints(18);
            Cell subtitleCell = subtitleRow.createCell(0);
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
            subtitleCell.setCellValue("Report Generated: " + currentDate);
            subtitleCell.setCellStyle(subtitleStyle);

            // Merge Row 1 across Columns A to F (0 to 5)
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

            // Row 3: Table Headers
            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(24);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Row 4+: Data Rows
            int rowIdx = 4;
            for (Asset asset : assets) {
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(20);

                // ID
                Cell c0 = row.createCell(0);
                c0.setCellValue(asset.getId() != null ? asset.getId() : 0);
                c0.setCellStyle(centerAlignStyle);

                // Name
                Cell c1 = row.createCell(1);
                c1.setCellValue(asset.getName() != null ? asset.getName() : "");
                c1.setCellStyle(leftAlignStyle);

                // Type
                Cell c2 = row.createCell(2);
                c2.setCellValue(asset.getType() != null ? asset.getType() : "");
                c2.setCellStyle(leftAlignStyle);

                // Asset Tag
                Cell c3 = row.createCell(3);
                c3.setCellValue(asset.getAssetTag() != null ? asset.getAssetTag() : "");
                c3.setCellStyle(centerAlignStyle);

                // Location
                Cell c4 = row.createCell(4);
                c4.setCellValue(asset.getLocation() != null ? asset.getLocation() : "");
                c4.setCellStyle(leftAlignStyle);

                // Status
                Cell c5 = row.createCell(5);
                c5.setCellValue(asset.getStatus() != null ? asset.getStatus() : "");
                c5.setCellStyle(centerAlignStyle);
            }

            // --- AUTO-SIZE & PADDING ---
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
                // Add width padding so text isn't tightly touching borders
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, currentWidth + 1200);
            }

            // Explicit extra width for Column B ("Asset Name / Model")
            sheet.setColumnWidth(1, sheet.getColumnWidth(1) + 1500);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private CellStyle createBaseDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Light Gray Thin Borders
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        
        return style;
    }
}