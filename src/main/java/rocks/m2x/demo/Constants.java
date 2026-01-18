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
     * Watermark text for draft documents
     */
    public static final String WATERMARK_DRAFT = "!!! DRAFT !!!";

    /**
     * Filename used when uploading DOCX to LibreOffice conversion service
     */
    public static final String LIBREOFFICE_UPLOAD_FILENAME = "document.docx";

    /**
     * Default format parameter value
     */
    public static final String DEFAULT_FORMAT = FILE_EXT_DOCX;
}
