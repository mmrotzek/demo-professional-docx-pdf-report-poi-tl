package rocks.m2x.demo.mcp.model;

import lombok.Builder;
import lombok.Data;

/**
 * Options for report generation
 */
@Data
@Builder
public class ReportOptions {
    /**
     * Whether the document should be read-only protected
     */
    @Builder.Default
    Boolean readonly = true;

    /**
     * Whether to enforce field updates on next open
     */
    @Builder.Default
    Boolean enforceUpdateFields = false;

    /**
     * Whether to add draft watermark
     */
    @Builder.Default
    Boolean draft = false;

    /**
     * Fields that should be rendered as HTML (comma-separated field names)
     */
    String htmlFields;
}
