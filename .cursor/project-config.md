# Project Configuration

This file contains project-specific configuration for automated commands and scripts.

## Maven Configuration

### Maven Wrapper Command
```bash
MAVEN_WRAPPER=./mvnw
```

**Note:** On Unix/Linux systems, use `bash ./mvnw` if direct execution fails due to permissions.

## Application Configuration

### Application Port
```yaml
APPLICATION_PORT: 8080
```

Default Spring Boot port. Not explicitly configured in `application.properties`.

### Health Endpoint
```yaml
HEALTH_ENDPOINT: /actuator/health
```

**Note:** Spring Boot Actuator is not explicitly configured in this project. The endpoint may not be available unless actuator dependencies are added.

### Swagger UI Path
```yaml
SWAGGER_UI_PATH: none
```

Swagger/OpenAPI is not configured in this project.

## Maven Profiles

### Architecture Tests
```yaml
ARCHITECTURE_TESTS_PROFILE: none
```

Architecture tests profile is not configured in `pom.xml`.

### Unit Tests
```yaml
UNIT_TESTS_PROFILE: none
```

Unit tests are run via default Maven Surefire plugin without a specific profile.

### Integration Tests
```yaml
INTEGRATION_TESTS_PROFILE: none
```

Integration tests profile is not configured in `pom.xml`.

**Note:** This project uses Docker Compose for the `docx2pdf` service, but integration tests are not configured as a Maven profile.

### Security Dependency Check
```yaml
SECURITY_CHECK_PROFILE: none
```

OWASP dependency check profile is not configured in `pom.xml`.

### OpenRewrite Profile
```yaml
OPENREWRITE_PROFILE: none
```

OpenRewrite profile is not configured in `pom.xml`.

## Docker Compose Configuration

### Docker Compose File
```yaml
DOCKER_COMPOSE_FILE: compose.yml
```

### Docker Compose Startup Command
```bash
DOCKER_COMPOSE_STARTUP_COMMAND: docker compose up -d
```

### Docker Compose Shutdown Command
```bash
DOCKER_COMPOSE_SHUTDOWN_COMMAND: docker compose down
```

### Service Wait Time
```yaml
DOCKER_COMPOSE_WAIT_TIME: 60
```

Time in seconds to wait for services to be healthy after startup.

### Services
- **docx2pdf**: LibreOffice-based DOCX to PDF conversion service
  - Image: `moalhaddar/docx-to-pdf:2.1.0-12`
  - Port: `7700:8080` (host:container)
  - Environment: `POOL_SIZE=1`

**Note:** The service is automatically managed by Spring Boot Docker Compose support when running the application.

## Key Endpoints for Manual Testing

### API Base Path
```
/api/soa
```

### Report Endpoint
```
GET /api/soa/report
```

**Parameters:**
- `format` (optional): `docx` (default) or `pdf`
  - Example: `http://localhost:8080/api/soa/report?format=pdf`
- `download` (optional): `true` (default) or `false`
  - `true`: Downloads the file with `Content-Disposition: attachment`
  - `false`: Opens the file inline with `Content-Disposition: inline`
  - Example: `http://localhost:8080/api/soa/report?format=pdf&download=false`

**Response:**
- Content-Type: `application/vnd.openxmlformats-officedocument.wordprocessingml.document` (DOCX) or `application/pdf` (PDF)
- Content-Disposition: Based on `download` parameter
- Body: Binary file content

**Examples:**
- DOCX download: `http://localhost:8080/api/soa/report`
- PDF download: `http://localhost:8080/api/soa/report?format=pdf`
- PDF inline: `http://localhost:8080/api/soa/report?format=pdf&download=false`

## Application Configuration Properties

Configuration is managed via `application.properties` and `ApplicationConfigurationProperties` class.

### Key Configuration Prefix
```
m2x.demo
```

### Template Configuration
```properties
m2x.demo.export.template=classpath:/templates/template_table.docx
```

Available templates:
- `classpath:/templates/template.docx` - Portrait template
- `classpath:/templates/template_table.docx` - Landscape table template (default)

### PDF Conversion Configuration
```properties
# Default: LibreOffice (via docx2pdf service)
m2x.demo.export.pdf-conversion.pdf-conversion=LIBREOFFICE
m2x.demo.export.pdf-conversion.libre-office.url=http://localhost:7700/pdf

# Alternative: Microsoft Graph API (not yet implemented)
# m2x.demo.export.pdf-conversion.pdf-conversion=graph-api
# m2x.demo.export.pdf-conversion.graph-api.tenant-id=
# m2x.demo.export.pdf-conversion.graph-api.client-id=
# m2x.demo.export.pdf-conversion.graph-api.client-secret=
# m2x.demo.export.pdf-conversion.graph-api.user-principal-name-or-id=
```

### CORS Configuration
```properties
# CORS configuration via properties (currently empty by default)
# m2x.demo.cors.allowed-origins=
```

CORS is configured in `SecurityConfig` class for `/api/**` paths.

## Project Structure

### Main Application Class
```
src/main/java/rocks/m2x/demo/DemoApplication.java
```

### Controllers
```
src/main/java/rocks/m2x/demo/controller/SoaController.java
```

### Services
```
src/main/java/rocks/m2x/demo/service/
```

### Configuration
```
src/main/java/rocks/m2x/demo/config/
```

### Templates
```
src/main/resources/templates/
```

## Additional Notes

- **Java Version:** 25 (LTS)
- **Spring Boot Version:** 3.5.9
- **Build Tool:** Maven
- **Docker Compose:** Used for docx2pdf service (automatically started by Spring Boot Docker Compose support)
- **Testing:** JUnit 5 with Spring Boot Test (default Maven Surefire plugin)
