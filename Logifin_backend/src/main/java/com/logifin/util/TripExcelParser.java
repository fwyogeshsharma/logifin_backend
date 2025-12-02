package com.logifin.util;

import com.logifin.dto.BulkUploadResponseDTO;
import com.logifin.dto.ErrorRowDTO;
import com.logifin.dto.TripRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing CSV and Excel files for bulk trip upload.
 */
@Component
@Slf4j
public class TripExcelParser {

    private static final int EWAY_BILL_NUMBER_COL = 0;
    private static final int PICKUP_COL = 1;
    private static final int DESTINATION_COL = 2;
    private static final int SENDER_COL = 3;
    private static final int RECEIVER_COL = 4;
    private static final int TRANSPORTER_COL = 5;
    private static final int LOAN_AMOUNT_COL = 6;
    private static final int INTEREST_RATE_COL = 7;
    private static final int MATURITY_DAYS_COL = 8;
    private static final int DISTANCE_KM_COL = 9;
    private static final int LOAD_TYPE_COL = 10;
    private static final int WEIGHT_KG_COL = 11;
    private static final int NOTES_COL = 12;

    /**
     * Parse CSV file and return list of TripRequestDTO
     */
    public List<TripRequestDTO> parseCsv(MultipartFile file, BulkUploadResponseDTO response) {
        List<TripRequestDTO> trips = new ArrayList<>();
        int rowNumber = 1; // Start from 1 (header is row 0)

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Skip empty lines
                if (!StringUtils.hasText(line.trim())) {
                    continue;
                }

                try {
                    String[] values = parseCsvLine(line);
                    TripRequestDTO trip = parseCsvRow(values, rowNumber, response);
                    if (trip != null) {
                        trips.add(trip);
                    }
                } catch (Exception e) {
                    log.error("Error parsing CSV row {}: {}", rowNumber, e.getMessage());
                    response.addError(ErrorRowDTO.parsingError(rowNumber, line, "Parse error: " + e.getMessage()));
                }
            }
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", e.getMessage());
            response.addError(ErrorRowDTO.parsingError(0, null, "Error reading CSV file: " + e.getMessage()));
        }

        return trips;
    }

    /**
     * Parse Excel file (XLS or XLSX) and return list of TripRequestDTO
     */
    public List<TripRequestDTO> parseExcel(MultipartFile file, BulkUploadResponseDTO response) {
        List<TripRequestDTO> trips = new ArrayList<>();

        String filename = file.getOriginalFilename();
        boolean isXlsx = filename != null && filename.toLowerCase().endsWith(".xlsx");

        try (Workbook workbook = isXlsx ?
                new XSSFWorkbook(file.getInputStream()) :
                new HSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) { // Start from 1 to skip header
                Row row = sheet.getRow(rowNum);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                int displayRowNum = rowNum + 1; // 1-indexed for display
                try {
                    TripRequestDTO trip = parseExcelRow(row, displayRowNum, response);
                    if (trip != null) {
                        trips.add(trip);
                    }
                } catch (Exception e) {
                    log.error("Error parsing Excel row {}: {}", displayRowNum, e.getMessage());
                    response.addError(ErrorRowDTO.parsingError(displayRowNum, rowToString(row),
                            "Parse error: " + e.getMessage()));
                }
            }
        } catch (IOException e) {
            log.error("Error reading Excel file: {}", e.getMessage());
            response.addError(ErrorRowDTO.parsingError(0, null, "Error reading Excel file: " + e.getMessage()));
        }

        return trips;
    }

    /**
     * Parse a CSV row into TripRequestDTO
     */
    private TripRequestDTO parseCsvRow(String[] values, int rowNumber, BulkUploadResponseDTO response) {
        List<String> errors = new ArrayList<>();

        // Validate required fields
        String ewayBillNumber = getValueOrEmpty(values, EWAY_BILL_NUMBER_COL);
        if (!StringUtils.hasText(ewayBillNumber)) {
            errors.add("E-way Bill Number is required");
        }

        String pickup = getValueOrEmpty(values, PICKUP_COL);
        if (!StringUtils.hasText(pickup)) {
            errors.add("Pickup location is required");
        }

        String destination = getValueOrEmpty(values, DESTINATION_COL);
        if (!StringUtils.hasText(destination)) {
            errors.add("Destination is required");
        }

        String sender = getValueOrEmpty(values, SENDER_COL);
        if (!StringUtils.hasText(sender)) {
            errors.add("Sender name is required");
        }

        String receiver = getValueOrEmpty(values, RECEIVER_COL);
        if (!StringUtils.hasText(receiver)) {
            errors.add("Receiver name is required");
        }

        String transporter = getValueOrEmpty(values, TRANSPORTER_COL);
        if (!StringUtils.hasText(transporter)) {
            errors.add("Transporter name is required");
        }

        // Parse numeric fields
        BigDecimal loanAmount = parseDecimal(getValueOrEmpty(values, LOAN_AMOUNT_COL), "Loan Amount", errors);
        if (loanAmount != null && loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Loan Amount must be greater than 0");
        }

        BigDecimal interestRate = parseDecimal(getValueOrEmpty(values, INTEREST_RATE_COL), "Interest Rate", errors);
        if (interestRate != null && (interestRate.compareTo(BigDecimal.ZERO) < 0 || interestRate.compareTo(BigDecimal.valueOf(100)) > 0)) {
            errors.add("Interest Rate must be between 0 and 100");
        }

        Integer maturityDays = parseInteger(getValueOrEmpty(values, MATURITY_DAYS_COL), "Maturity Days", errors);
        if (maturityDays != null && (maturityDays < 1 || maturityDays > 365)) {
            errors.add("Maturity Days must be between 1 and 365");
        }

        // Optional fields
        BigDecimal distanceKm = parseDecimalOptional(getValueOrEmpty(values, DISTANCE_KM_COL));
        String loadType = getValueOrEmpty(values, LOAD_TYPE_COL);
        BigDecimal weightKg = parseDecimalOptional(getValueOrEmpty(values, WEIGHT_KG_COL));
        String notes = getValueOrEmpty(values, NOTES_COL);

        // If there are validation errors, add to response and return null
        if (!errors.isEmpty()) {
            ErrorRowDTO errorRow = ErrorRowDTO.builder()
                    .rowNumber(rowNumber)
                    .ewayBillNumber(ewayBillNumber)
                    .errorType(ErrorRowDTO.ErrorType.VALIDATION_ERROR)
                    .errors(errors)
                    .build();
            response.addError(errorRow);
            return null;
        }

        return TripRequestDTO.builder()
                .ewayBillNumber(ewayBillNumber)
                .pickup(pickup)
                .destination(destination)
                .sender(sender)
                .receiver(receiver)
                .transporter(transporter)
                .loanAmount(loanAmount)
                .interestRate(interestRate)
                .maturityDays(maturityDays)
                .distanceKm(distanceKm)
                .loadType(loadType)
                .weightKg(weightKg)
                .notes(notes)
                .build();
    }

    /**
     * Parse an Excel row into TripRequestDTO
     */
    private TripRequestDTO parseExcelRow(Row row, int rowNumber, BulkUploadResponseDTO response) {
        List<String> errors = new ArrayList<>();

        // Validate required fields
        String ewayBillNumber = getCellStringValue(row.getCell(EWAY_BILL_NUMBER_COL));
        if (!StringUtils.hasText(ewayBillNumber)) {
            errors.add("E-way Bill Number is required");
        }

        String pickup = getCellStringValue(row.getCell(PICKUP_COL));
        if (!StringUtils.hasText(pickup)) {
            errors.add("Pickup location is required");
        }

        String destination = getCellStringValue(row.getCell(DESTINATION_COL));
        if (!StringUtils.hasText(destination)) {
            errors.add("Destination is required");
        }

        String sender = getCellStringValue(row.getCell(SENDER_COL));
        if (!StringUtils.hasText(sender)) {
            errors.add("Sender name is required");
        }

        String receiver = getCellStringValue(row.getCell(RECEIVER_COL));
        if (!StringUtils.hasText(receiver)) {
            errors.add("Receiver name is required");
        }

        String transporter = getCellStringValue(row.getCell(TRANSPORTER_COL));
        if (!StringUtils.hasText(transporter)) {
            errors.add("Transporter name is required");
        }

        // Parse numeric fields
        BigDecimal loanAmount = getCellDecimalValue(row.getCell(LOAN_AMOUNT_COL), "Loan Amount", errors);
        if (loanAmount != null && loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Loan Amount must be greater than 0");
        }

        BigDecimal interestRate = getCellDecimalValue(row.getCell(INTEREST_RATE_COL), "Interest Rate", errors);
        if (interestRate != null && (interestRate.compareTo(BigDecimal.ZERO) < 0 || interestRate.compareTo(BigDecimal.valueOf(100)) > 0)) {
            errors.add("Interest Rate must be between 0 and 100");
        }

        Integer maturityDays = getCellIntegerValue(row.getCell(MATURITY_DAYS_COL), "Maturity Days", errors);
        if (maturityDays != null && (maturityDays < 1 || maturityDays > 365)) {
            errors.add("Maturity Days must be between 1 and 365");
        }

        // Optional fields
        BigDecimal distanceKm = getCellDecimalValueOptional(row.getCell(DISTANCE_KM_COL));
        String loadType = getCellStringValue(row.getCell(LOAD_TYPE_COL));
        BigDecimal weightKg = getCellDecimalValueOptional(row.getCell(WEIGHT_KG_COL));
        String notes = getCellStringValue(row.getCell(NOTES_COL));

        // If there are validation errors, add to response and return null
        if (!errors.isEmpty()) {
            ErrorRowDTO errorRow = ErrorRowDTO.builder()
                    .rowNumber(rowNumber)
                    .ewayBillNumber(ewayBillNumber)
                    .errorType(ErrorRowDTO.ErrorType.VALIDATION_ERROR)
                    .errors(errors)
                    .build();
            response.addError(errorRow);
            return null;
        }

        return TripRequestDTO.builder()
                .ewayBillNumber(ewayBillNumber)
                .pickup(pickup)
                .destination(destination)
                .sender(sender)
                .receiver(receiver)
                .transporter(transporter)
                .loanAmount(loanAmount)
                .interestRate(interestRate)
                .maturityDays(maturityDays)
                .distanceKm(distanceKm)
                .loadType(loadType)
                .weightKg(weightKg)
                .notes(notes)
                .build();
    }

    // ==================== Helper Methods ====================

    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentValue.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        values.add(currentValue.toString().trim());

        return values.toArray(new String[0]);
    }

    private String getValueOrEmpty(String[] values, int index) {
        if (values != null && index < values.length) {
            return values[index] != null ? values[index].trim() : "";
        }
        return "";
    }

    private BigDecimal parseDecimal(String value, String fieldName, List<String> errors) {
        if (!StringUtils.hasText(value)) {
            errors.add(fieldName + " is required");
            return null;
        }
        try {
            return new BigDecimal(value.trim().replaceAll("[,\\s]", ""));
        } catch (NumberFormatException e) {
            errors.add(fieldName + " must be a valid number");
            return null;
        }
    }

    private BigDecimal parseDecimalOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim().replaceAll("[,\\s]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value, String fieldName, List<String> errors) {
        if (!StringUtils.hasText(value)) {
            errors.add(fieldName + " is required");
            return null;
        }
        try {
            return Integer.parseInt(value.trim().replaceAll("[,\\s]", ""));
        } catch (NumberFormatException e) {
            errors.add(fieldName + " must be a valid integer");
            return null;
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                // Handle numeric values without scientific notation
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private BigDecimal getCellDecimalValue(Cell cell, String fieldName, List<String> errors) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            errors.add(fieldName + " is required");
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim().replaceAll("[,\\s]", "");
                return new BigDecimal(value);
            }
            errors.add(fieldName + " must be a valid number");
            return null;
        } catch (NumberFormatException e) {
            errors.add(fieldName + " must be a valid number");
            return null;
        }
    }

    private BigDecimal getCellDecimalValueOptional(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (!StringUtils.hasText(value)) {
                    return null;
                }
                return new BigDecimal(value.replaceAll("[,\\s]", ""));
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getCellIntegerValue(Cell cell, String fieldName, List<String> errors) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            errors.add(fieldName + " is required");
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim().replaceAll("[,\\s]", "");
                return Integer.parseInt(value);
            }
            errors.add(fieldName + " must be a valid integer");
            return null;
        } catch (NumberFormatException e) {
            errors.add(fieldName + " must be a valid integer");
            return null;
        }
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (int i = 0; i <= NOTES_COL; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellStringValue(cell);
                if (StringUtils.hasText(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String rowToString(Row row) {
        if (row == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= NOTES_COL; i++) {
            if (i > 0) sb.append(", ");
            sb.append(getCellStringValue(row.getCell(i)));
        }
        return sb.toString();
    }
}
