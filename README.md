# docCreator

docCreator is a Spring Boot REST service that generates sample PDF files for selected PDF versions and PDF standards.

The service provides one endpoint per requested version/profile and returns the generated PDF directly as HTTP response.

## Project status

Current focus:
- Generate PDF examples for PDF versions 1.0 to 2.0.
- Generate PDF examples for PDF/A, PDF/X, PDF/UA and PDF/VT profiles.
- Run validation checks for every generated file.
- Run veraPDF CLI checks for PDF/A and PDF/UA profiles.

Removed legacy parts:
- Old XML/XSL-FO creator endpoint and service.
- Old PDF-to-HTML converter endpoint and service.
- Obsolete custom config class for file-path based conversion.

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