# Architecture Rules and Patterns

Quick reference guide for architectural rules and coding conventions used in this project.

## Quick Reference

| Pattern | Location | Key Rule |
|---------|----------|----------|
| **Layered Architecture** | All layers | Controllers → Services → Data Models (no cross-dependencies) |
| **Dependency Injection** | All services | Constructor injection with `@RequiredArgsConstructor`, final fields |
| **Configuration** | `config` package | `@ConfigurationProperties` with prefix `m2x.demo`, nested classes |
| **Data Models** | `service.*.data` | Lombok `@Data` + `@Builder`, simple POJOs |
| **Constants** | `Constants.java` | All magic strings centralized |
| **Exceptions** | `service.exc` | Custom checked/unchecked exceptions, generic handling in controllers |
| **Resources** | All services | Always use try-with-resources |

---

## Core Patterns

### 1. Layered Architecture

```
Controller → Service → Data Models
```

**Rules:**
- Controllers handle HTTP only (request/response, headers)
- Services contain business logic
- Data models are simple POJOs (no business logic)
- No circular dependencies

### 2. Dependency Injection

```java
@Service
@RequiredArgsConstructor  // ✅ Constructor injection
@Slf4j
public class MyService {
    private final OtherService dependency;  // ✅ final field
}
```

**Rules:**
- ✅ Constructor injection with `@RequiredArgsConstructor`
- ✅ Mark dependencies as `final`
- ❌ Never use `@Autowired` on fields
- ❌ Never use setter injection

### 3. Configuration Pattern

```java
@ConfigurationProperties(prefix = "m2x.demo")
public class ApplicationConfigurationProperties {
    Export export = new Export();  // Defaults provided
    
    @Getter @Setter
    public static class Export {
        Resource template = ...;  // Default value
        boolean readonly = true;
    }
}
```

**Rules:**
- Nested static classes for grouping
- Provide defaults in property classes
- Enable with `@EnableConfigurationProperties` in main class

### 4. Data Models

```java
@Data
@Builder
public class SoA {
    String version;
    boolean draft;
    List<ControlGroup> groups;
}

// Usage
SoA.builder().version("1.1").draft(false).build();
```

**Rules:**
- Use `@Data` + `@Builder`
- No business logic in models
- Builder pattern for construction

### 5. Constants

All magic strings in `Constants.java`:
- `DATE_FORMAT` = `"yyyy-MM-dd"`
- `FILE_EXT_DOCX`, `FILE_EXT_PDF`
- `CONTENT_TYPE_*`, `FILE_PREFIX_DRAFT`, `WATERMARK_DRAFT`
- `TEMPLATE_FIELD_*`, `LIBREOFFICE_UPLOAD_FILENAME`

**Rule:** Never hardcode strings that appear in multiple places.

### 6. Exception Handling

**Exception Hierarchy:**
- `InvalidConfigurationException` (checked)
- `PdfConversionException` (checked)
- `GraphApiException` (unchecked) - includes errorIdentifier + statusCode

**Pattern:**
```java
// Service throws specific exceptions
public void method() throws PdfConversionException { }

// Controller catches generically
catch (Exception e) {
    log.error("Error", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
}
```

### 7. Resource Management

```java
try (InputStream is = resource.getInputStream()) {
    try (XWPFTemplate t = XWPFTemplate.compile(is, config)) {
        // use resource
    }
}
```

**Rule:** Always use try-with-resources for AutoCloseable resources.

---

## Domain-Specific Rules

### File Naming

**Pattern:** `{PREFIX}{BASE_NAME}_{VERSION}.{EXTENSION}`

- `PREFIX`: `_DRAFT_` if draft, else `""`
- `BASE_NAME`: `SoA`
- `VERSION`: from `SoA.getVersion()`
- `EXTENSION`: `docx` or `pdf`

**Examples:**
- `SoA_1.1.docx`
- `_DRAFT_SoA_1.0.pdf`

### HTTP Response

**Required headers:**
```java
response.setContentType(Constants.CONTENT_TYPE_PDF);  // or DOCX
response.setContentLength(fileContent.length);
response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
    download ? "attachment; filename=" + fileName : "inline; filename=" + fileName);
```

**Content-Type mapping:**
- `pdf` → `application/pdf`
- `docx` → `application/vnd.openxmlformats-officedocument.wordprocessingml.document`

### Template Rendering

**Configuration:**
- Templates: `classpath:/templates/`
- Config property: `m2x.demo.export.template`
- HTML fields: Use `HtmlRenderPolicy` for `description`, `company`
- Control numbering: `{groupNr}.{controlNr}` (e.g., "5.1")

### PDF Conversion (Strategy Pattern)

```java
enum PdfConverter { LIBREOFFICE, GRAPH_API }

if (converter == LIBREOFFICE) {
    return libreOfficeService.convert(data, config);
} else if (converter == GRAPH_API) {
    return graphApiService.convert(data, config);
}
```

**Rule:** Validate config with `Objects.requireNonNull()`, wrap NPE in `InvalidConfigurationException`.

### Document Protection

Applied based on config/data:
- Read-only: `export.readonly = true`
- Update fields: `export.enforceUpdateFields = true`
- Draft watermark: `SoA.isDraft() == true` → adds `WATERMARK_DRAFT`

### Return Values

Services return `Pair<SoA, ByteArrayOutputStream>` when both metadata and content needed:
- Left: Data model (for filename generation)
- Right: ByteArrayOutputStream (for HTTP response)

### API Endpoints

**Base path:** `/api/soa`

**Query parameters:**
| Parameter | Default | Values | Description |
|-----------|---------|--------|-------------|
| `format`  | `docx`  | `docx`, `pdf` | Output format |
| `download`| `true`  | `true`, `false` | Attachment vs inline |

### Service Annotations

All services must have:
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MyService {
    final Dependency dependency;
}
```

**Logging:**
- `log.debug()` for important operations
- `log.error()` with full stack traces for exceptions

### Date Formatting

- Format: `Constants.DATE_FORMAT` (`yyyy-MM-dd`)
- Use `LocalDate` (not `Date`/`Calendar`)
- Auto-set `created` during render: `LocalDate.now().format(...)`

### CORS

CORS enabled only when `allowedOrigins` is non-empty:
```java
if (allowedOrigins != null && allowedOrigins.length > 0) {
    // configure CORS
}
```

**Rule:** Disabled by default, applies to `/api/**`.

---

## Principles Summary

1. **Separation of Concerns** - Clear layer boundaries
2. **Dependency Injection** - Constructor-based, final fields
3. **Configuration-driven** - Externalize with defaults
4. **Resource Safety** - Always try-with-resources
5. **Type Safety** - Enums, typed config, builder pattern
6. **Error Handling** - Specific exceptions, generic controller handling
7. **Consistency** - Follow naming conventions throughout

---

## Adding New Features

When adding new code:
- ✅ Follow layered architecture (Controller → Service → Data)
- ✅ Use existing patterns (builder, strategy, configuration properties)
- ✅ Extract constants for magic strings
- ✅ Use try-with-resources
- ✅ Constructor injection with `@RequiredArgsConstructor`
- ✅ Log errors with full stack traces
