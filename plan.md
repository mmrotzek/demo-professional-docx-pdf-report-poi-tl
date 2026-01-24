## Security remediation plan (for later execution)

### Context
- MCP is local/stdio only (no remote HTTP exposure required).
- Azure/OneDrive PDF conversion can be removed.
- Focus areas: file system safety, data exposure prevention, and hardening.

### Goals
- Eliminate arbitrary file read/write/delete risks in template handling.
- Remove external document upload flows (Azure/OneDrive).
- Reduce potential data leakage and harden parsing of untrusted templates.

### Plan (ordered)

1) Remove Azure/OneDrive conversion path
   - Delete PdfOneDrivePersonalService and Ms365Client.
   - Remove Graph API config from ApplicationConfigurationProperties and
     application.properties.
   - Update DocxToPdfService to only use LibreOffice converter.
   - Remove microsoft-graph, microsoft-graph-beta, azure-identity, and
     msal4j from pom.xml.
   - Remove any tests tied to OneDrive/Graph conversion.

2) Harden template storage against path traversal
   - Generate a server-side UUID for stored templates; do not use user input
     as a filesystem name.
   - Reject template names containing path separators.
   - Enforce canonical-path checks before any write/delete operation.
   - Store template metadata with both logical ID and safe filename.

3) Restrict template resolution to safe sources
   - Remove filesystem-path support from resolveTemplateSource.
   - Allow only:
     - classpath:templates/<name>
     - templates created by provide_template (stored in temp dir)
   - Consider an allowlist of known template names.

4) Prevent cross-session template collisions/leakage
   - Remove the "inline-template" fallback ID; always generate a unique ID.
   - Scope list_templates to local session or disable it by default.
   - Add explicit cleanup after report generation if a template was inline.

5) Guard against zip-bombs / oversized templates
   - Enforce HTTP payload limits where applicable.
   - Set POI zip security limits (max inflate ratio, entry size, total size).
   - Keep template size limit conservative and consistent with parsing limits.

6) Reduce sensitive logging and error leakage
   - Avoid logging absolute file paths for templates.
   - Return generic errors to callers; keep stack traces server-side.

### Testing / verification checklist
- Generate report with inline template (docx) still works.
- Generate report with classpath template still works.
- Template path traversal inputs are rejected.
- No file writes outside temp dir are possible.
- No Graph/OneDrive code paths remain.

### Risks / notes
- Removing filesystem-path template support may impact existing workflows.
- POI security limits must be tuned to avoid rejecting valid large templates.
