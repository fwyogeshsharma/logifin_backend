package com.logifin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DTO representing an error row from bulk upload.
 * Contains row number, original data, and error messages.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Error details for a failed row in bulk upload")
public class ErrorRowDTO {

    @Schema(description = "Row number in the uploaded file (1-indexed)", example = "5")
    private int rowNumber;

    @Schema(description = "E-way Bill Number from the row (if available)", example = "EWB123456789")
    private String ewayBillNumber;

    @Schema(description = "List of validation/parsing errors for this row")
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Schema(description = "Raw data from the row (for debugging)")
    private String rawData;

    @Schema(description = "Error type/category", example = "VALIDATION_ERROR")
    private ErrorType errorType;

    /**
     * Error type enumeration
     */
    public enum ErrorType {
        VALIDATION_ERROR,
        DUPLICATE_EWAY_BILL,
        PARSING_ERROR,
        MISSING_REQUIRED_FIELD,
        INVALID_FORMAT,
        DATABASE_ERROR
    }

    /**
     * Helper method to add an error message
     */
    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    /**
     * Create an ErrorRowDTO for a validation error
     */
    public static ErrorRowDTO validationError(int rowNumber, String ewayBillNumber, String error) {
        return ErrorRowDTO.builder()
                .rowNumber(rowNumber)
                .ewayBillNumber(ewayBillNumber)
                .errorType(ErrorType.VALIDATION_ERROR)
                .errors(new ArrayList<>(Collections.singletonList(error)))
                .build();
    }

    /**
     * Create an ErrorRowDTO for a duplicate e-way bill
     */
    public static ErrorRowDTO duplicateError(int rowNumber, String ewayBillNumber) {
        return ErrorRowDTO.builder()
                .rowNumber(rowNumber)
                .ewayBillNumber(ewayBillNumber)
                .errorType(ErrorType.DUPLICATE_EWAY_BILL)
                .errors(new ArrayList<>(Collections.singletonList("E-way Bill Number already exists: " + ewayBillNumber)))
                .build();
    }

    /**
     * Create an ErrorRowDTO for a parsing error
     */
    public static ErrorRowDTO parsingError(int rowNumber, String rawData, String error) {
        return ErrorRowDTO.builder()
                .rowNumber(rowNumber)
                .rawData(rawData)
                .errorType(ErrorType.PARSING_ERROR)
                .errors(new ArrayList<>(Collections.singletonList(error)))
                .build();
    }

    /**
     * Create an ErrorRowDTO for a missing required field
     */
    public static ErrorRowDTO missingFieldError(int rowNumber, String fieldName) {
        return ErrorRowDTO.builder()
                .rowNumber(rowNumber)
                .errorType(ErrorType.MISSING_REQUIRED_FIELD)
                .errors(new ArrayList<>(Collections.singletonList("Missing required field: " + fieldName)))
                .build();
    }
}
