package rocks.m2x.demo.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.springframework.stereotype.Component;

/**
 * Configuration for Apache POI zip security limits to prevent zip-bomb attacks.
 * DOCX files are ZIP archives, so we need to configure limits on ZIP entry sizes
 * and compression ratios.
 */
@Component
@Slf4j
public class PoiSecurityConfig {

    /**
     * Maximum ratio between compressed and uncompressed data (default: 0.01 = 1%)
     * This prevents zip-bomb attacks where a small compressed file expands to huge size.
     */
    private static final double MIN_INFLATE_RATIO = 0.01;

    /**
     * Maximum size of a single ZIP entry (100MB)
     * This prevents individual entries from being too large.
     */
    private static final long MAX_ENTRY_SIZE = 100L * 1024 * 1024; // 100 MB

    /**
     * Maximum size of text extracted from ZIP entries (50MB)
     * This prevents excessive text extraction that could be used in attacks.
     */
    private static final long MAX_TEXT_SIZE = 50L * 1024 * 1024; // 50 MB

    @PostConstruct
    public void configurePoiZipSecurity() {
        // Set minimum inflate ratio to prevent zip-bomb attacks
        // A ratio of 0.01 means compressed data must be at least 1% of uncompressed size
        // This prevents attacks where a small compressed file expands to huge size
        ZipSecureFile.setMinInflateRatio(MIN_INFLATE_RATIO);
        log.info("Configured POI zip security: minInflateRatio={}", MIN_INFLATE_RATIO);

        // Set maximum entry size to prevent individual entries from being too large
        ZipSecureFile.setMaxEntrySize(MAX_ENTRY_SIZE);
        log.info("Configured POI zip security: maxEntrySize={} bytes ({} MB)", MAX_ENTRY_SIZE, MAX_ENTRY_SIZE / (1024 * 1024));

        // Set maximum text size to prevent excessive text extraction
        ZipSecureFile.setMaxTextSize(MAX_TEXT_SIZE);
        log.info("Configured POI zip security: maxTextSize={} bytes ({} MB)", MAX_TEXT_SIZE, MAX_TEXT_SIZE / (1024 * 1024));
    }
}
