# docCreator

docCreator is a Spring Boot REST service that generates sample PDF files for selected PDF versions and PDF standards.

The service provides one endpoint per requested version/profile and returns the generated PDF directly as HTTP response.

## Project status

Current focus:
- Generate PDF examples for PDF versions 1.0 to 2.0.
- Generate PDF examples for PDF/A, PDF/X, PDF/UA and PDF/VT profiles.
- Run validation checks for every generated file.
- Run veraPDF CLI checks for PDF/A and PDF/UA profiles.
- Embed a variety of font types (TrueType and Type 1/PostScript) and generated demo images in
  every profile's font/image demo page.

Done so far:
- Endpoints and content generation for all PDF version and PDF/A/X/UA/VT targets listed below.
- Font demo page with TrueType fonts (Latin/Cyrillic/Greek via Liberation + Noto Sans, CJK via a
  reduced Noto Sans SC subset) and a genuine Type 1 (PostScript) font (URW Nimbus Sans), always
  embedded in full since PDFBox cannot subset simple Type 1 fonts.
- Runtime-generated demo images (no bundled image files, so no image licensing concerns).
- General validation checks (header version, readability, marker text, profile-family rules) plus
  veraPDF-backed PDF/A and PDF/UA compliance checks.
- 49 unit tests covering font embedding, content builders (one parameterized case per `PdfTarget`),
  and image generation — all currently passing (`mvn test`).

### Open issues / known limitations

- **PDF/A-1 compliance of the Type 1 font is not empirically verified.** veraPDF is not installed
  in the current development environment, so the real compliance checker was never actually run
  against the Type 1 demo font. Type 1 is spec-wise one of the oldest/most universally supported
  PDF font formats (unlike OpenType/CFF, which PDFBox 2.0.31/3.0.3 cannot embed as a new Type0 font
  at all — confirmed by source inspection), but this remains an assumption until veraPDF is
  actually run. **Action needed:** install veraPDF locally (see Configuration below) and re-check
  `/api/pdf/standard/pdf-a-1a` (and the other PDF/A targets).
- **`checkHeader()` locale bug** in `PdfValidationService`: `String.format("%.1f", ...)` uses the
  JVM default locale. On a machine with a comma-decimal locale (e.g. German), it produces
  `"%PDF-1,7"` instead of the spec-correct `"%PDF-1.7"`, so the `HEADER_VERSION` check always fails
  there even though the generated PDF is correct. Fix: use `String.format(Locale.US, "%.1f", ...)`.
- **`checkContainsTargetText()` is byte-scan based**: it searches for marker text directly in the
  raw (possibly FlateDecode-compressed) PDF bytes instead of the decoded content stream, so it can
  spuriously fail depending on whether/how content streams end up compressed.
- **`X-Validation-Details` header can go missing from responses.** When the underlying check
  message contains a character that isn't ISO-8859-1-encodable (e.g. from truncated veraPDF XML
  output), Tomcat throws `UnmappableCharacterException` while writing response headers, and the
  header is silently dropped from the actual HTTP response. Not yet fixed; validation details
  should probably be sanitized before being placed in a header, or moved into the response body.

None of the three validation-check issues above were introduced by the font work — they were
discovered as a side effect of end-to-end testing and are pre-existing gaps in
`PdfValidationService`/veraPDF wiring, not in font handling itself.

### Next steps

- Install veraPDF locally and confirm real PDF/A-1a/1b compliance for the Type 1 font; adjust the
  font demo (e.g. skip Type 1 for specific targets) if a genuine rule violation turns up.
- Fix the `HEADER_VERSION` locale bug (`Locale.US`).
- Make `checkContainsTargetText()` decode content streams instead of scanning raw bytes.
- Sanitize/rework `X-Validation-Details` so non-Latin1 content can't silently drop the header.
- Continue adding more embedded element variety (further font styles, additional image types)
  per the phased plan, with a review checkpoint after each phase.


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

## Fonts and licenses

All fonts are bundled as classpath resources under `src/main/resources/fonts/` and embedded via
`de.hardt.docCreator.service.font.PdfFontService` / `FontAsset`. TrueType fonts are embedded as
subsets; the Type 1 font is always embedded in full (PDFBox 2.0.31 has no subsetting support for
simple, non-CID Type 1 fonts).

| Font | Format | Coverage | License | Notes |
|---|---|---|---|---|
| Liberation Sans/Serif/Mono (Regular/Bold/Italic/BoldItalic) | TrueType | Latin | [SIL Open Font License 1.1](https://scripts.sil.org/OFL) (`fonts/liberation/OFL.txt`) | Metric-compatible with Arial/Times New Roman/Courier New |
| Noto Sans (Regular) | TrueType | Latin, Cyrillic, Greek | SIL OFL 1.1 (`fonts/noto/OFL-NotoSans.txt`) | Single file covering three scripts for the multi-script demo |
| Noto Sans SC (reduced subset) | TrueType | CJK (demo glyphs only) | SIL OFL 1.1 (`fonts/noto/OFL-NotoSansSC.txt`) | Pre-subsetted to a small glyph set to keep the repository size down |
| URW Nimbus Sans (Regular) | Type 1 (PostScript) | Latin | AGPL-3.0, with an explicit font-embedding exception (`fonts/urw-nimbus/LICENSE.txt`, full license text in `fonts/urw-nimbus/COPYING.txt`) | Metric-compatible clone of Helvetica; genuine Adobe Type 1 font program, sourced as raw/PFA-style Type 1 data and converted to PFB at runtime by `Type1PfaToPfbConverter` (PDFBox's embedder only accepts PFB) |

Demo images used in the profile pages are generated at runtime (not bundled files), so they carry
no separate licensing concerns.

The font-embedding exception in the URW Nimbus Sans license explicitly permits including the font
in generated PDF/PostScript documents regardless of the license of the document itself, which is
why it is safe to use here despite the otherwise copyleft AGPL-3.0 license.

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