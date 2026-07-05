package de.hardt.docCreator.controller;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hardt.docCreator.model.GeneratedPdfResult;
import de.hardt.docCreator.model.PdfTarget;
import de.hardt.docCreator.service.PdfProfileService;

@RestController
public class PdfGenerationController {

    private final PdfProfileService pdfProfileService;

    public PdfGenerationController(PdfProfileService pdfProfileService) {
        this.pdfProfileService = pdfProfileService;
    }

    @GetMapping("/api/pdf/version/1.0")
    public ResponseEntity<?> pdfVersion10() { return generate(PdfTarget.V_1_0); }

    @GetMapping("/api/pdf/version/1.1")
    public ResponseEntity<?> pdfVersion11() { return generate(PdfTarget.V_1_1); }

    @GetMapping("/api/pdf/version/1.2")
    public ResponseEntity<?> pdfVersion12() { return generate(PdfTarget.V_1_2); }

    @GetMapping("/api/pdf/version/1.3")
    public ResponseEntity<?> pdfVersion13() { return generate(PdfTarget.V_1_3); }

    @GetMapping("/api/pdf/version/1.4")
    public ResponseEntity<?> pdfVersion14() { return generate(PdfTarget.V_1_4); }

    @GetMapping("/api/pdf/version/1.5")
    public ResponseEntity<?> pdfVersion15() { return generate(PdfTarget.V_1_5); }

    @GetMapping("/api/pdf/version/1.6")
    public ResponseEntity<?> pdfVersion16() { return generate(PdfTarget.V_1_6); }

    @GetMapping("/api/pdf/version/1.7")
    public ResponseEntity<?> pdfVersion17() { return generate(PdfTarget.V_1_7); }

    @GetMapping("/api/pdf/version/2.0")
    public ResponseEntity<?> pdfVersion20() { return generate(PdfTarget.V_2_0); }

    @GetMapping("/api/pdf/standard/pdf-a-1a")
    public ResponseEntity<?> pdfA1a() { return generate(PdfTarget.PDF_A_1A); }

    @GetMapping("/api/pdf/standard/pdf-a-1b")
    public ResponseEntity<?> pdfA1b() { return generate(PdfTarget.PDF_A_1B); }

    @GetMapping("/api/pdf/standard/pdf-a-2a")
    public ResponseEntity<?> pdfA2a() { return generate(PdfTarget.PDF_A_2A); }

    @GetMapping("/api/pdf/standard/pdf-a-2b")
    public ResponseEntity<?> pdfA2b() { return generate(PdfTarget.PDF_A_2B); }

    @GetMapping("/api/pdf/standard/pdf-a-2u")
    public ResponseEntity<?> pdfA2u() { return generate(PdfTarget.PDF_A_2U); }

    @GetMapping("/api/pdf/standard/pdf-a-3a")
    public ResponseEntity<?> pdfA3a() { return generate(PdfTarget.PDF_A_3A); }

    @GetMapping("/api/pdf/standard/pdf-a-3b")
    public ResponseEntity<?> pdfA3b() { return generate(PdfTarget.PDF_A_3B); }

    @GetMapping("/api/pdf/standard/pdf-a-3u")
    public ResponseEntity<?> pdfA3u() { return generate(PdfTarget.PDF_A_3U); }

    @GetMapping("/api/pdf/standard/pdf-a-4")
    public ResponseEntity<?> pdfA4() { return generate(PdfTarget.PDF_A_4); }

    @GetMapping("/api/pdf/standard/pdf-a-4e")
    public ResponseEntity<?> pdfA4e() { return generate(PdfTarget.PDF_A_4E); }

    @GetMapping("/api/pdf/standard/pdf-a-4f")
    public ResponseEntity<?> pdfA4f() { return generate(PdfTarget.PDF_A_4F); }

    @GetMapping("/api/pdf/standard/pdf-x-1a-2001")
    public ResponseEntity<?> pdfX1a2001() { return generate(PdfTarget.PDF_X_1A_2001); }

    @GetMapping("/api/pdf/standard/pdf-x-3-2002")
    public ResponseEntity<?> pdfX32002() { return generate(PdfTarget.PDF_X_3_2002); }

    @GetMapping("/api/pdf/standard/pdf-x-4")
    public ResponseEntity<?> pdfX4() { return generate(PdfTarget.PDF_X_4); }

    @GetMapping("/api/pdf/standard/pdf-x-4p")
    public ResponseEntity<?> pdfX4p() { return generate(PdfTarget.PDF_X_4P); }

    @GetMapping("/api/pdf/standard/pdf-x-5g")
    public ResponseEntity<?> pdfX5g() { return generate(PdfTarget.PDF_X_5G); }

    @GetMapping("/api/pdf/standard/pdf-x-5n")
    public ResponseEntity<?> pdfX5n() { return generate(PdfTarget.PDF_X_5N); }

    @GetMapping("/api/pdf/standard/pdf-x-5pg")
    public ResponseEntity<?> pdfX5pg() { return generate(PdfTarget.PDF_X_5PG); }

    @GetMapping("/api/pdf/standard/pdf-ua-1")
    public ResponseEntity<?> pdfUa1() { return generate(PdfTarget.PDF_UA_1); }

    @GetMapping("/api/pdf/standard/pdf-ua-2")
    public ResponseEntity<?> pdfUa2() { return generate(PdfTarget.PDF_UA_2); }

    @GetMapping("/api/pdf/standard/pdf-vt-1")
    public ResponseEntity<?> pdfVt1() { return generate(PdfTarget.PDF_VT_1); }

    @GetMapping("/api/pdf/standard/pdf-vt-2")
    public ResponseEntity<?> pdfVt2() { return generate(PdfTarget.PDF_VT_2); }

    private ResponseEntity<?> generate(PdfTarget target) {
        try {
            GeneratedPdfResult result = pdfProfileService.generate(target);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename(result.fileName()).build());
            headers.add("X-Pdf-Target", target.displayName());
            headers.add("X-Validation-Result", result.report().valid() ? "PASS" : "FAIL");
            headers.add("X-Validation-Summary", result.report().summarize());
            headers.add("X-Validation-Details", result.report().checks().stream()
                    .map(check -> check.code() + ":" + (check.passed() ? "PASS" : "FAIL") + "-" + check.message())
                    .collect(Collectors.joining("|")));
            return new ResponseEntity<>(result.bytes(), headers, HttpStatus.OK);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse("GENERATION_ERROR", ex.getMessage(), target.displayName()));
        }
    }

    public record ErrorResponse(String code, String message, String target) {
    }
}
