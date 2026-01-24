package rocks.m2x.demo.mcp.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import rocks.m2x.demo.mcp.model.ValidationError;
import rocks.m2x.demo.mcp.model.ValidationResult;
import rocks.m2x.demo.service.mcp.McpReportService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP tool for data validation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataValidationTool {
    private final McpReportService mcpReportService;

    @McpTool(
            name = "validate_data",
            description = "Validate report data against template requirements. " +
                    "If the template has a JSON schema defined, validation is performed against that schema. " +
                    "Otherwise, basic validation is performed (e.g., checking for required fields)."
    )
    public ValidateDataResult validateData(
            @McpToolParam(description = "Template identifier to validate against", required = true)
            String templateId,
            @McpToolParam(description = "Report data as JSON object to validate", required = true)
            Map<String, Object> reportData) {
        try {
            ValidationResult result = mcpReportService.validateData(templateId, reportData);

            List<ValidationErrorInfo> errors = result.getErrors().stream()
                    .map(error -> ValidationErrorInfo.builder()
                            .field(error.getField())
                            .message(error.getMessage())
                            .code(error.getCode())
                            .rejectedValue(error.getRejectedValue() != null ? error.getRejectedValue().toString() : null)
                            .build())
                    .collect(Collectors.toList());

            return ValidateDataResult.builder()
                    .valid(result.isValid())
                    .errors(errors)
                    .warnings(result.getWarnings())
                    .errorCount(result.getErrors().size())
                    .warningCount(result.getWarnings().size())
                    .message(result.isValid() ? "Data is valid" : "Validation failed with " + result.getErrors().size() + " error(s)")
                    .build();

        } catch (SecurityException e) {
            log.warn("Security violation in validate_data: {}", e.getMessage());
            return ValidateDataResult.builder()
                    .valid(false)
                    .error("Invalid template identifier")
                    .message("Template identifier is invalid or not allowed")
                    .build();
        } catch (Exception e) {
            log.error("Failed to validate data", e);
            return ValidateDataResult.builder()
                    .valid(false)
                    .error("Validation failed")
                    .message("Failed to validate data. Please check template identifier and data.")
                    .build();
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class ValidateDataResult {
        boolean valid;
        List<ValidationErrorInfo> errors;
        List<String> warnings;
        int errorCount;
        int warningCount;
        String message;
        String error;
    }

    @lombok.Data
    @lombok.Builder
    public static class ValidationErrorInfo {
        String field;
        String message;
        String code;
        String rejectedValue;
    }
}
