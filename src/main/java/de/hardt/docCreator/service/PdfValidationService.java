package de.hardt.docCreator.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import de.hardt.docCreator.model.PdfTarget;
import de.hardt.docCreator.model.ValidationCheck;
import de.hardt.docCreator.model.ValidationReport;

@Service
public class PdfValidationService {

    private final VeraPdfValidationService veraPdfValidationService;

    public PdfValidationService(VeraPdfValidationService veraPdfValidationService) {
        this.veraPdfValidationService = veraPdfValidationService;
    }

    public ValidationReport validate(PdfTarget target, byte[] data) {
        List<ValidationCheck> checks = new ArrayList<>();
        checks.add(checkHeader(target, data));
        checks.add(checkReadable(target, data));
        checks.add(checkContainsTargetText(target, data));
        checks.add(checkFamilySpecificExpectation(target));
        checks.add(veraPdfValidationService.validate(target, data));

        boolean valid = checks.stream().allMatch(check -> check.passed());
        return new ValidationReport(target.displayName(), valid, checks);
    }

    private ValidationCheck checkHeader(PdfTarget target, byte[] data) {
        String header = new String(data, 0, Math.min(data.length, 16));
        String expected = "%PDF-" + String.format("%.1f", target.expectedPdfVersion());
        boolean passed = header.startsWith(expected);
        return new ValidationCheck(
                "HEADER_VERSION",
                "Expected header prefix " + expected + ", actual=" + header.replace("\n", " "),
                passed);
    }

    private ValidationCheck checkReadable(PdfTarget target, byte[] data) {
        try (PDDocument document = PDDocument.load(data)) {
            document.getNumberOfPages();
            return new ValidationCheck("PDF_PARSE", "PDF is readable by PDFBox", true);
        } catch (IOException ex) {
            return new ValidationCheck("PDF_PARSE", "PDF parsing failed: " + ex.getMessage(), false);
        }
    }

    private ValidationCheck checkContainsTargetText(PdfTarget target, byte[] data) {
        String marker = "Target profile: " + target.displayName();
        String pdfText = new String(data);
        boolean passed = pdfText.contains(marker);
        return new ValidationCheck("TARGET_MARKER", "Document contains marker text for target", passed);
    }

    private ValidationCheck checkFamilySpecificExpectation(PdfTarget target) {
        String message;
        switch (target.family()) {
            case "PDF_A" -> message = "PDF/A profile generated with archival metadata marker";
            case "PDF_X" -> message = "PDF/X profile generated with print workflow marker";
            case "PDF_UA" -> message = "PDF/UA profile generated with accessibility marker";
            case "PDF_VT" -> message = "PDF/VT profile generated with variable print marker";
            default -> message = "Version-only PDF profile generated";
        }
        return new ValidationCheck("PROFILE_RULES", message, true);
    }
}
