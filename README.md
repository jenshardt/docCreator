# docCreator

docCreator is a Spring Boot REST service that generates sample PDF files for selected PDF versions and PDF standards.

The service provides one endpoint per requested version/profile and returns the generated PDF directly as HTTP response.

## Project status

Current focus:
- Generate PDF examples for PDF versions 1.0 to 2.0.
- Generate PDF examples for PDF/A, PDF/X, PDF/UA and PDF/VT profiles.
- Run validation checks for every generated file.
- Run veraPDF CLI checks for PDF/A and PDF/UA profiles.


## REST endpoints

### PDF version endpoints
- GET /api/pdf/version/1.0
- GET /api/pdf/version/1.1
- GET /api/pdf/version/1.2
- GET /api/pdf/version/1.3
- GET /api/pdf/version/1.4
- GET /api/pdf/version/1.5
- GET /api/pdf/version/1.6
- GET /api/pdf/version/1.7
- GET /api/pdf/version/2.0

### PDF/A endpoints
- GET /api/pdf/standard/pdf-a-1a
- GET /api/pdf/standard/pdf-a-1b
- GET /api/pdf/standard/pdf-a-2a
- GET /api/pdf/standard/pdf-a-2b
- GET /api/pdf/standard/pdf-a-2u
- GET /api/pdf/standard/pdf-a-3a
- GET /api/pdf/standard/pdf-a-3b
- GET /api/pdf/standard/pdf-a-3u
- GET /api/pdf/standard/pdf-a-4
- GET /api/pdf/standard/pdf-a-4e
- GET /api/pdf/standard/pdf-a-4f

### PDF/X endpoints
- GET /api/pdf/standard/pdf-x-1a-2001
- GET /api/pdf/standard/pdf-x-3-2002
- GET /api/pdf/standard/pdf-x-4
- GET /api/pdf/standard/pdf-x-4p
- GET /api/pdf/standard/pdf-x-5g
- GET /api/pdf/standard/pdf-x-5n
- GET /api/pdf/standard/pdf-x-5pg

### PDF/UA endpoints
- GET /api/pdf/standard/pdf-ua-1
- GET /api/pdf/standard/pdf-ua-2

### PDF/VT endpoints
- GET /api/pdf/standard/pdf-vt-1
- GET /api/pdf/standard/pdf-vt-2

## Response behavior

Success:
- HTTP 200
- Content-Type: application/pdf
- Content-Disposition attachment with generated file name

Validation headers:
- X-Pdf-Target
- X-Validation-Result (PASS or FAIL)
- X-Validation-Summary
- X-Validation-Details

Error:
- HTTP 400
- JSON body with fields: code, message, target

## Validation concept

General checks:
- PDF header version check
- Readability/parsing check via PDFBox
- Marker-content check
- Profile-family expectation check

veraPDF checks:
- Applied for PDF/A and PDF/UA targets
- Executed via configured CLI command
- Result integrated as VERAPDF check in validation details

## Configuration

Application properties are defined in src/main/resources/application.properties.

Current keys:
- vera-pdf.enabled=true
- vera-pdf.executable=verapdf

Windows example with absolute path:
- vera-pdf.executable=C:\\tools\\verapdf\\verapdf.bat

## Main components

- de.hardt.docCreator.controller.PdfGenerationController
- de.hardt.docCreator.service.PdfProfileService
- de.hardt.docCreator.service.PdfValidationService
- de.hardt.docCreator.service.VeraPdfValidationService
- de.hardt.docCreator.model.PdfTarget

## Run

- mvn spring-boot:run

## API request examples in VS Code (REST Client)

If you use the VS Code extension `REST Client` (`humao.rest-client`), request examples are included in the project under `http/`.

Structured request files:
- `http/pdf-versions.http`
- `http/pdf-a.http`
- `http/pdf-x.http`
- `http/pdf-ua.http`
- `http/pdf-vt.http`

Optional root entry file:
- `requests.http` (contains a smoke test and links to the structured files)

How to use:
1. Start the application (`mvn spring-boot:run`).
2. Open one of the `.http` files from `http/`.
3. Click `Send Request` above any request block.
4. Inspect status, headers and body in the REST Client response panel.

Notes:
- All requests use `@baseUrl = http://localhost:8080`.
- Successful responses return `application/pdf` and validation metadata in headers (`X-Pdf-Target`, `X-Validation-Result`, `X-Validation-Summary`, `X-Validation-Details`).

## Download PDFs as files (PowerShell)

To download generated PDFs directly as files, use:
- `scripts/download-pdfs.ps1`

Examples:
1. Download all endpoints to `downloads/`:
	- `powershell -ExecutionPolicy Bypass -File .\scripts\download-pdfs.ps1`
2. Download only selected targets:
	- `powershell -ExecutionPolicy Bypass -File .\scripts\download-pdfs.ps1 -Only pdf-ua-1,pdf-a-2b`
3. Use custom API URL and output folder:
	- `powershell -ExecutionPolicy Bypass -File .\scripts\download-pdfs.ps1 -BaseUrl http://localhost:8080 -OutputDir out-pdfs`

Target names for `-Only` correspond to file names, for example:
- `pdf-version-1.7`
- `pdf-a-2b`
- `pdf-x-4`
- `pdf-ua-1`
- `pdf-vt-1`