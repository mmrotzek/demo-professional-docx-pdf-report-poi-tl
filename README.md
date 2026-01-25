# Professional Template-Based Report Generator

![Build Status](https://github.com/mmrotzek/demo-professional-docx-pdf-report-poi-tl/actions/workflows/build-and-push-docker.yml/badge.svg)
![Docker Image Version](https://img.shields.io/github/v/tag/mmrotzek/demo-professional-docx-pdf-report-poi-tl?label=version)
![License](https://img.shields.io/github/license/mmrotzek/demo-professional-docx-pdf-report-poi-tl)
![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green?logo=springboot)

This demo shows how to generate professional Word template-based reports from any structured data. Transform your JSON data into polished DOCX or PDF reports using customizable Word templates.

**Key Features:**

- **Generic Report Generation**: Generate reports from any template and data structure - not limited to specific use cases
- **Dynamic Data & Templates**: Combine structured JSON data with customizable Word templates to generate professional reports
- **MCP Server Integration**: Built-in Model Context Protocol (MCP) server enables AI agents and IDEs (like Cursor, Claude Desktop) to generate reports programmatically
- **Test Web UI**: Interactive web interface at `/ui/reports` for testing template uploads and report generation without writing code
- **Multiple Output Formats**: Generate reports as DOCX or PDF with a single API call
- **Template Management**: Upload and manage templates dynamically
- **Data Validation**: Validate report data against template schemas
- **HTML Rendering**: Configurable HTML rendering for specific fields
- **Document Options**: Read-only protection, draft watermarks, field updates

**Example Use Case**: This repository includes a **Statement of Applicability (SoA)** report for ISO 27001 as a working example, demonstrating the capabilities with a real-world scenario.

> **IMPORTANT**: The example data was generated with AI/GPT tools and is not a real-world example.

Related blog post: [https://m2x.rocks/poi-tl-professionelle-berichte-aus-word-templates/](https://m2x.rocks/poi-tl-professionelle-berichte-aus-word-templates/)

**Table of Contents:**

- [Usage](#usage)
- [Screenshots](#screenshots)
- [Developer Documentation](#developer-documentation)
    - [Want to try yourself?! - Out of the Box Demo](#want-to-try-yourself---out-of-the-box-demo)
    - [Template Placeholders in Word Documents](#template-placeholders-in-word-documents)
    - [MCP](#mcp)

## Usage

Run the MCP server with docker compose as described below.

> **Note:** this service is intended for demo and development purposes only. For production use, please adapt the configuration and security settings accordingly.

1. Get the `compose.yml` file and run it:
    ```shell
    curl -o compose.yml -O https://raw.githubusercontent.com/mmrotzek/demo-professional-docx-pdf-report-poi-tl/refs/heads/main/compose.prod.yml
   ```
   ```shell
    docker compose up -d
    ```
2. This starts the MCP server on port 8080 - `http://localhost:8080/mcp/message` (MCP Streamable HTTP: JSON-RPC over HTTP with optional streaming responses) - and `docx2pdf` conversion service on port 7700.
3. You can test the report generation service using the built-in **Web UI**: [http://localhost:8080/ui/reports](http://localhost:8080/ui/reports) - a HTML interface for generating dynamic reports with template upload and JSON data input.

This application includes an MCP (Model Context Protocol) server that enables AI agents (like Claude, GPT, or Cursor IDE) to generate professional DOCX/PDF reports using custom templates and structured data.

Templates are located in [src/main/resources/templates](src/main/resources/templates).

### Example: Configure MCP Server for Cursor IDE

_When server is running on localhost:8080_:

1. **Option 1: Project-specific configuration** (recommended)
    - Copy `.cursor/mcp.json.example` to `.cursor/mcp.json` in this project
    - Or create `.cursor/mcp.json` with the configuration below

2. **Option 2: Global configuration**
    - Create or update `~/.cursor/mcp.json` on your system

Configuration content:

```json
{
  "mcpServers": {
    "document-report-generator": {
      "url": "http://localhost:8080/mcp/message",
      "transport": "streamableHttp"
    }
  }
}
```

**Important**:
- Make sure the application is running on `localhost:8080` before connecting
- After adding the configuration, restart Cursor IDE
- The MCP server tools (`generate_report`, `provide_template`, `validate_data`, `list_templates`) will be available in Cursor's AI chat

> **Note**: The server uses Streamable HTTP transport which is compatible with Cursor IDE.

## Screenshots

### Portrait

_Word Template_ - [src/main/resources/templates/template.docx](src/main/resources/templates/template_soa.docx)

| Front Page                                                                                                                       | Other Pages                                                                                                        |
|----------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| [![template front page](example/template_portrait_frontpage.png "Portrait Front Page")](example/template_portrait_frontpage.png) | [![template_page](example/template_portrait_page.png "Portrait  Other Pages")](example/template_portrait_page.png) |

_Rendered as DOCX_ - Full document: [example/portrait.docx](example/SoA_1.1_portrait.docx)

[![template front page](example/portrait_docx.png "Portrait Rendered as DOCX")](example/portrait_docx.png)

_Rendered as PDF_ - Full report: [example/portrait.pdf](example/SoA_1.1_portrait.pdf)

|                                                                                                           |                                                                                                         |
|-----------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| [![template front page](example/portrait_pdf.png "Portrait Rendered as PDF Front Page")](example/portrait_pdf.png) | [![template_page](example/portrait_pdf_2.png "Portrait Rendered as PDF Other Page")](example/portrait_pdf_2.png) |

### Landscape

_Word Template_ - [src/main/resources/templates/template_table.docx](src/main/resources/templates/template_soa_table.docx)

| Front Page                                                                                                                          | Other Pages                                                                                                |
|-------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| [![template front page](example/template_landscape_frontpage.png "Landscape Front Page")](example/template_landscape_frontpage.png) | [![template_page](example/template_landscape_page.png "Landscape Other Pages")](example/template_landscape_page.png) |

_Rendered as DOCX_ - Full document: [example/landscape.docx](example/SoA_1.0_landscape.docx)

[![template front page](example/landscape_docx.png "Landscape Rendered as DOCX")](example/landscape_docx.png)

_Rendered as PDF_ - Full report: [example/landscape.pdf](example/SoA_1.0_landscape.pdf)

[![template front page](example/landscape_pdf.png "Landscape Rendered as PDF")](example/landscape_pdf.png)

---

## Developer Documentation

### Want to try yourself?! - Out of the Box Demo

This is a Spring Boot application that uses [poi-tl](https://github.com/Sayi/poi-tl) for Word document generation and [moalhaddar/docx-to-pdf](https://github.com/moalhaddar/docx-to-pdf) for PDF conversion via REST API.

#### Requirements

Docker is required for PDF conversion.

#### Run

```shell
docker compose up -d
```

`./mvnw spring-boot:run` - or run the [DemoApplication](src/main/java/rocks/m2x/demo/DemoApplication.java) class in IDEA (requires Lombok enabled).

#### Usage

**Web UI**: [http://localhost:8080/ui/reports](http://localhost:8080/ui/reports) - HTML interface for generating dynamic reports with template upload and JSON data input.

Templates are located in `src/main/resources/templates`.


### Template Placeholders in Word Documents

Creating templates is simple - just open Microsoft Word and type placeholders using double curly braces:

- **Simple values**: `{{customerName}}`, `{{price}}`, `{{date}}`
- **Nested objects**: `{{customer.name}}`, `{{customer.address.city}}`
- **Lists/Tables**: `{{?items}}{{description}}{{/items}}` (loops iterate over array items)

**That's it!** Save as .docx and use it to generate reports.

**Advanced**: To render HTML content (bold, italic, lists, etc.), specify fields in the `htmlFields` option with HTML tags: `<p>`, `<b>`, `<i>`, `<u>`, `<ul>`, `<ol>`, `<li>`, `<br/>`.

### MCP 

#### Configuration

MCP server settings in `application.properties`:

```properties
m2x.demo.mcp.enabled=true
m2x.demo.mcp.stdio-enabled=true
m2x.demo.mcp.http-enabled=true
m2x.demo.mcp.http-path=/mcp/message
m2x.demo.mcp.max-template-size=10485760
m2x.demo.mcp.template-ttl=PT1H

# Spring AI MCP Server Configuration
spring.ai.mcp.server.enabled=true
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.protocol=STREAMABLE
spring.ai.mcp.server.annotation-scanner.enabled=true
# Streamable HTTP endpoint configuration
spring.ai.mcp.server.streamable-http.mcp-endpoint=/mcp/message
```

#### MCP Tools

The MCP server provides the following tools for AI agents:

##### `generate_report`

Generate DOCX/PDF reports from structured data and templates.

**Parameters:**
- `templateId` (optional): Template identifier or path (e.g., "template_soa.docx")
- `templateContent` (optional): Base64 encoded .docx template (for dynamic templates)
- `reportData` (required): JSON object matching template placeholders
- `outputFormat` (optional): "docx" or "pdf" (default: "docx")
- `options` (optional): Report options object:
    - `readonly` (boolean): Enable read-only protection
    - `draft` (boolean): Add draft watermark
    - `htmlFields` (string): Comma-separated list of fields to render as HTML

**Returns:** Base64 encoded document

##### `provide_template`

Upload and register a template for later use in report generation.

**Parameters:**
- `templateName` (required): Unique template identifier (will be used as templateId)
- `templateContent` (required): Base64 encoded .docx file
- `description` (optional): Human-readable template description

**Returns:** Template registration confirmation with templateId

##### `validate_data`

Validate report data against template requirements before generation.

**Parameters:**
- `templateId` (required): Template identifier to validate against
- `reportData` (required): JSON object to validate

**Returns:** Validation result with any errors or warnings

##### `list_templates`

Get list of available templates with metadata.

**Returns:** Array of template objects with id, name, description, and size information

#### Resources

- `template://schema/{templateId}` - JSON Schema defining expected data structure
- `template://info/{templateId}` - Template metadata (name, description, version)
- `sample://data/{templateId}` - Example data structure for a template


### Security Considerations

- **Apache POI Zip Security**: Configured limits to prevent zip-bomb attacks:
  - Minimum inflate ratio: 1% (prevents highly compressed files from expanding to huge sizes)
  - Maximum entry size: 100MB per ZIP entry
  - Maximum text size: 50MB for extracted text
- **Template Content Validation**: Validates .docx files have proper ZIP signature to prevent malicious files
- **File Size Limits**: Base64 template content limited to 10MB (configurable via `m2x.demo.mcp.max-template-size`)
- **Temporary File Cleanup**: Automatic cleanup with configurable TTL (default: 1 hour via `m2x.demo.mcp.template-ttl`)
- **Path Traversal Protection**: 
  - Template names validated to reject path separators, traversal sequences (`..`), and absolute paths
  - Server-generated UUIDs used for template IDs (never user input)
  - Canonical path validation ensures files stay within designated directories
  - Classpath template allowlist prevents arbitrary resource loading
