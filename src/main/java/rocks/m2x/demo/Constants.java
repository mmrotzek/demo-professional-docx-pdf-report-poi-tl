package rocks.m2x.demo;

/**
 * Application-wide constants.
 * <p>
 * This class contains all magic strings and hardcoded values used throughout the application.
 * Constants should be used instead of hardcoding values to ensure consistency and maintainability.
 */
public final class Constants {

    private Constants() {
        // Utility class - prevent instantiation
    }

    /**
     * Date format used for SoA creation dates.
     * Format: yyyy-MM-dd (e.g., "2024-01-15")
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * File extensions
     */
    public static final String FILE_EXT_DOCX = "docx";
    public static final String FILE_EXT_PDF = "pdf";

    /**
     * HTTP Content-Type values
     */
    public static final String CONTENT_TYPE_PDF = "application/pdf";
    public static final String CONTENT_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    /**
     * File naming constants
     */
    public static final String FILE_PREFIX_DRAFT = "_DRAFT_";
    public static final String FILE_BASE_NAME = "SoA";

    /**
     * Watermark text for draft documents
     */
    public static final String WATERMARK_DRAFT = "!!! DRAFT !!!";

    /**
     * Template field names that should be rendered as HTML
     */
    public static final String TEMPLATE_FIELD_DESCRIPTION = "description";
    public static final String TEMPLATE_FIELD_COMPANY = "company";

    /**
     * Filename used when uploading DOCX to LibreOffice conversion service
     */
    public static final String LIBREOFFICE_UPLOAD_FILENAME = "document.docx";

    /**
     * Default format parameter value
     */
    public static final String DEFAULT_FORMAT = FILE_EXT_DOCX;
}
