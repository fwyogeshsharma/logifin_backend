package com.logifin.util;

import com.logifin.dto.BulkUploadResponseDTO;
import com.logifin.dto.ErrorRowDTO;
import com.logifin.dto.TripRequestDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TripExcelParser Tests")
class TripExcelParserTest {

    private TripExcelParser tripExcelParser;
    private BulkUploadResponseDTO response;

    @BeforeEach
    void setUp() {
        tripExcelParser = new TripExcelParser();
        response = BulkUploadResponseDTO.builder()
                .successfulTripIds(new ArrayList<>())
                .errors(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("CSV Parsing Tests")
    class CsvParsingTests {

        @Test
        @DisplayName("Should parse valid CSV file successfully")
        void parseCsv_ValidFile_Success() throws IOException {
            String csvContent = "pickup,destination,sender,receiver,transporter,loanAmount,interestRate,maturityDays,distanceKm,loadType,weightKg,notes\n" +
                    "Mumbai,Delhi,ABC Traders,XYZ Industries,Fast Logistics,100000,12.5,30,1400,Electronics,5000,Handle with care\n" +
                    "Chennai,Bangalore,Sender B,Receiver B,Quick Transport,200000,10.0,45,350,Textiles,3000,Fragile items";

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    csvContent.getBytes()
            );

            List<TripRequestDTO> result = tripExcelParser.parseCsv(file, response);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPickup()).isEqualTo("Mumbai");
            assertThat(result.get(0).getDestination()).isEqualTo("Delhi");
            assertThat(result.get(0).getLoanAmount()).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(result.get(1).getPickup()).isEqualTo("Chennai");
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle CSV with missing required fields")
        void parseCsv_MissingRequiredFields() throws IOException {
            String csvContent = "pickup,destination,sender,receiver,transporter,loanAmount,interestRate,maturityDays\n" +
                    ",Delhi,ABC Traders,XYZ Industries,Fast Logistics,100000,12.5,30"; // Missing pickup

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    csvContent.getBytes()
            );

            List<TripRequestDTO> result = tripExcelParser.parseCsv(file, response);

            assertThat(result).isEmpty();
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0).getErrorType()).isEqualTo(ErrorRowDTO.ErrorType.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("Should handle CSV with invalid numeric values")
        void parseCsv_InvalidNumericValues() throws IOException {
            String csvContent = "pickup,destination,sender,receiver,transporter,loanAmount,interestRate,maturityDays\n" +
                    "Mumbai,Delhi,ABC Traders,XYZ Industries,Fast Logistics,invalid,12.5,30";

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    csvContent.getBytes()
            );

            List<TripRequestDTO> result = tripExcelParser.parseCsv(file, response);

            assertThat(result).isEmpty();
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0).getErrors()).contains("Loan Amount must be a valid number");
        }

        @Test
        @DisplayName("Should handle CSV with out-of-range values")
        void parseCsv_OutOfRangeValues() throws IOException {
            String csvContent = "pickup,destination,sender,receiver,transporter,loanAmount,interestRate,maturityDays\n" +
                    "Mumbai,Delhi,ABC Traders,XYZ Industries,Fast Logistics,100000,150,500"; // Interest > 100, Days > 365

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    csvContent.getBytes()
            );

            List<TripRequestDTO> result = tripExcelParser.parseCsv(file, response);

            assertThat(result).isEmpty();
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0).getErrors())
                    .contains("Interest Rate must be between 0 and 100")
                    .contains("Maturity Days must be between 1 and 365");
        }

        @Test
        @DisplayName("Should handle CSV with quoted fields containing commas")
        void parseCsv_QuotedFields() throws IOException {
            String csvContent = "pickup,destination,sender,receiver,transporter,loanAmount,interestRate,maturityDays,distanceKm,loadType,weightKg,notes\n" +
                    "\"Mumbai, Maharashtra\",\"Delhi, NCR\",\"ABC Traders, Inc.\",XYZ Industries,Fast Logistics,100000,12.5,30,1400,Electronics,5000,\"Handle with care, fragile\"";

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    csvContent.getBytes()
            );

            List<TripRequestDTO> result = tripExcelParser.parseCsv(file, response);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPickup()).isEqualTo("Mumbai, Maharashtra");
            assertThat(result.get(0).getDestination()).isEqualTo("Delhi, NCR");
            assertThat(result.get(0).getSender()).isEqualTo("ABC Traders, Inc.");
            assertThat(result.get(0).getNotes()).isEqualTo("Handle with care, fragile");
        }

        @Test
        @DisplayName("Should skip empty rows in CSV")
        void parseCsv_SkipEmptyRows() throws IOException {
            String csvContent = "pickup,destination,sender,receiver,transporter,loanAmount,interestRate,maturityDays\n" +
                    "Mumbai,Delhi,ABC Traders,XYZ Industries,Fast Logistics,100000,12.5,30\n" +
                    "\n" +
                    "   \n" +
                    "Chennai,Bangalore,Sender B,Receiver B,Quick Transport,200000,10.0,45";

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    csvContent.getBytes()
            );

            List<TripRequestDTO> result = tripExcelParser.parseCsv(file, response);

            assertThat(result).hasSize(2);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle negative loan amount")
        void parseCsv_NegativeLoanAmount() throws IOException {
            String csvContent = "pickup,destination,sender,receiver,transporter,loanAmount,interestRate,maturityDays\n" +
                    "Mumbai,Delhi,ABC Traders,XYZ Industries,Fast Logistics,-100000,12.5,30";

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    csvContent.getBytes()
            );

            List<TripRequestDTO> result = tripExcelParser.parseCsv(file, response);

            assertThat(result).isEmpty();
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0).getErrors()).contains("Loan Amount must be greater than 0");
        }
    }

    @Nested
    @DisplayName("Excel Parsing Tests")
    class ExcelParsingTests {

        @Test
        @DisplayName("Should parse valid Excel file successfully")
        void parseExcel_ValidFile_Success() throws IOException {
            byte[] excelContent = createValidExcelFile();
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    excelContent
            );

            List<TripRequestDTO> result = tripExcelParser.parseExcel(file, response);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPickup()).isEqualTo("Mumbai");
            assertThat(result.get(0).getLoanAmount()).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(result.get(1).getPickup()).isEqualTo("Chennai");
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle Excel with missing required fields")
        void parseExcel_MissingRequiredFields() throws IOException {
            byte[] excelContent = createExcelWithMissingFields();
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    excelContent
            );

            List<TripRequestDTO> result = tripExcelParser.parseExcel(file, response);

            assertThat(result).isEmpty();
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0).getErrorType()).isEqualTo(ErrorRowDTO.ErrorType.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("Should handle Excel with numeric values stored as text")
        void parseExcel_NumericValuesAsText() throws IOException {
            byte[] excelContent = createExcelWithTextNumbers();
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    excelContent
            );

            List<TripRequestDTO> result = tripExcelParser.parseExcel(file, response);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLoanAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        }

        @Test
        @DisplayName("Should skip empty rows in Excel")
        void parseExcel_SkipEmptyRows() throws IOException {
            byte[] excelContent = createExcelWithEmptyRows();
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    excelContent
            );

            List<TripRequestDTO> result = tripExcelParser.parseExcel(file, response);

            assertThat(result).hasSize(2);
            assertThat(response.getErrors()).isEmpty();
        }

        // Helper methods to create Excel files for testing

        private byte[] createValidExcelFile() throws IOException {
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Trips");

                // Header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"pickup", "destination", "sender", "receiver",
                        "transporter", "loanAmount", "interestRate", "maturityDays",
                        "distanceKm", "loadType", "weightKg", "notes"};
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                // Data row 1
                Row row1 = sheet.createRow(1);
                row1.createCell(0).setCellValue("Mumbai");
                row1.createCell(1).setCellValue("Delhi");
                row1.createCell(2).setCellValue("Sender A");
                row1.createCell(3).setCellValue("Receiver A");
                row1.createCell(4).setCellValue("Fast Logistics");
                row1.createCell(5).setCellValue(100000);
                row1.createCell(6).setCellValue(12.5);
                row1.createCell(7).setCellValue(30);
                row1.createCell(8).setCellValue(1400);
                row1.createCell(9).setCellValue("Electronics");
                row1.createCell(10).setCellValue(5000);
                row1.createCell(11).setCellValue("Handle with care");

                // Data row 2
                Row row2 = sheet.createRow(2);
                row2.createCell(0).setCellValue("Chennai");
                row2.createCell(1).setCellValue("Bangalore");
                row2.createCell(2).setCellValue("Sender B");
                row2.createCell(3).setCellValue("Receiver B");
                row2.createCell(4).setCellValue("Quick Transport");
                row2.createCell(5).setCellValue(200000);
                row2.createCell(6).setCellValue(10.0);
                row2.createCell(7).setCellValue(45);

                workbook.write(out);
                return out.toByteArray();
            }
        }

        private byte[] createExcelWithMissingFields() throws IOException {
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Trips");

                // Header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"pickup", "destination", "sender", "receiver",
                        "transporter", "loanAmount", "interestRate", "maturityDays"};
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                // Data row with missing pickup
                Row row1 = sheet.createRow(1);
                row1.createCell(0).setCellValue(""); // Empty pickup
                row1.createCell(1).setCellValue("Delhi");
                row1.createCell(2).setCellValue("Sender A");
                row1.createCell(3).setCellValue("Receiver A");
                row1.createCell(4).setCellValue("Fast Logistics");
                row1.createCell(5).setCellValue(100000);
                row1.createCell(6).setCellValue(12.5);
                row1.createCell(7).setCellValue(30);

                workbook.write(out);
                return out.toByteArray();
            }
        }

        private byte[] createExcelWithTextNumbers() throws IOException {
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Trips");

                // Header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"pickup", "destination", "sender", "receiver",
                        "transporter", "loanAmount", "interestRate", "maturityDays"};
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                // Data row with numbers as text
                Row row1 = sheet.createRow(1);
                row1.createCell(0).setCellValue("Mumbai");
                row1.createCell(1).setCellValue("Delhi");
                row1.createCell(2).setCellValue("Sender A");
                row1.createCell(3).setCellValue("Receiver A");
                row1.createCell(4).setCellValue("Fast Logistics");
                row1.createCell(5).setCellValue("100000"); // Text
                row1.createCell(6).setCellValue("12.5"); // Text
                row1.createCell(7).setCellValue("30"); // Text

                workbook.write(out);
                return out.toByteArray();
            }
        }

        private byte[] createExcelWithEmptyRows() throws IOException {
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Trips");

                // Header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"pickup", "destination", "sender", "receiver",
                        "transporter", "loanAmount", "interestRate", "maturityDays"};
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                // Data row 1
                Row row1 = sheet.createRow(1);
                row1.createCell(0).setCellValue("Mumbai");
                row1.createCell(1).setCellValue("Delhi");
                row1.createCell(2).setCellValue("Sender A");
                row1.createCell(3).setCellValue("Receiver A");
                row1.createCell(4).setCellValue("Fast Logistics");
                row1.createCell(5).setCellValue(100000);
                row1.createCell(6).setCellValue(12.5);
                row1.createCell(7).setCellValue(30);

                // Empty row 2
                sheet.createRow(2);

                // Empty row 3 with blank cells
                Row row3 = sheet.createRow(3);
                row3.createCell(0).setCellValue("");

                // Data row 4
                Row row4 = sheet.createRow(4);
                row4.createCell(0).setCellValue("Chennai");
                row4.createCell(1).setCellValue("Bangalore");
                row4.createCell(2).setCellValue("Sender B");
                row4.createCell(3).setCellValue("Receiver B");
                row4.createCell(4).setCellValue("Quick Transport");
                row4.createCell(5).setCellValue(200000);
                row4.createCell(6).setCellValue(10.0);
                row4.createCell(7).setCellValue(45);

                workbook.write(out);
                return out.toByteArray();
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty CSV file")
        void parseCsv_EmptyFile() throws IOException {
            String csvContent = "pickup,destination,sender,receiver,transporter,loanAmount,interestRate,maturityDays";

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    csvContent.getBytes()
            );

            List<TripRequestDTO> result = tripExcelParser.parseCsv(file, response);

            assertThat(result).isEmpty();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle CSV with only whitespace values")
        void parseCsv_WhitespaceValues() throws IOException {
            String csvContent = "pickup,destination,sender,receiver,transporter,loanAmount,interestRate,maturityDays\n" +
                    "   ,   ,   ,   ,   ,   ,   ,   ";

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    csvContent.getBytes()
            );

            List<TripRequestDTO> result = tripExcelParser.parseCsv(file, response);

            assertThat(result).isEmpty();
            assertThat(response.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("Should handle CSV with numbers containing commas")
        void parseCsv_NumbersWithCommas() throws IOException {
            String csvContent = "pickup,destination,sender,receiver,transporter,loanAmount,interestRate,maturityDays\n" +
                    "Mumbai,Delhi,ABC Traders,XYZ Industries,Fast Logistics,\"1,00,000\",12.5,30";

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    csvContent.getBytes()
            );

            List<TripRequestDTO> result = tripExcelParser.parseCsv(file, response);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLoanAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        }

        @Test
        @DisplayName("Should handle optional fields correctly")
        void parseCsv_OptionalFieldsMissing() throws IOException {
            String csvContent = "pickup,destination,sender,receiver,transporter,loanAmount,interestRate,maturityDays,distanceKm,loadType,weightKg,notes\n" +
                    "Mumbai,Delhi,ABC Traders,XYZ Industries,Fast Logistics,100000,12.5,30,,,, ";

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    csvContent.getBytes()
            );

            List<TripRequestDTO> result = tripExcelParser.parseCsv(file, response);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDistanceKm()).isNull();
            assertThat(result.get(0).getLoadType()).isEmpty();
            assertThat(result.get(0).getWeightKg()).isNull();
            assertThat(result.get(0).getNotes()).isEmpty();
        }

        @Test
        @DisplayName("Should handle maturity days boundary values")
        void parseCsv_MaturityDaysBoundary() throws IOException {
            String csvContent = "pickup,destination,sender,receiver,transporter,loanAmount,interestRate,maturityDays\n" +
                    "Mumbai,Delhi,Sender,Receiver,Transporter,100000,12.5,1\n" +
                    "Chennai,Bangalore,Sender,Receiver,Transporter,100000,12.5,365\n" +
                    "Pune,Hyderabad,Sender,Receiver,Transporter,100000,12.5,0\n" +
                    "Jaipur,Lucknow,Sender,Receiver,Transporter,100000,12.5,366";

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "trips.csv",
                    "text/csv",
                    csvContent.getBytes()
            );

            List<TripRequestDTO> result = tripExcelParser.parseCsv(file, response);

            assertThat(result).hasSize(2); // Only first two should be valid
            assertThat(result.get(0).getMaturityDays()).isEqualTo(1);
            assertThat(result.get(1).getMaturityDays()).isEqualTo(365);
            assertThat(response.getErrors()).hasSize(2); // Last two should have errors
        }
    }
}
