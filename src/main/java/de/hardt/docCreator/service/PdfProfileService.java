package de.hardt.docCreator.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import de.hardt.docCreator.model.GeneratedPdfResult;
import de.hardt.docCreator.model.PdfTarget;
import de.hardt.docCreator.model.ValidationReport;

@Service
public class PdfProfileService {

    private final PdfValidationService validationService;

    public PdfProfileService(PdfValidationService validationService) {
        this.validationService = validationService;
    }

    public GeneratedPdfResult generate(PdfTarget target) throws IOException {
        byte[] bytes = generatePdfBytes(target);
        ValidationReport report = validationService.validate(target, bytes);
        String fileName = target.slug() + ".pdf";
        return new GeneratedPdfResult(fileName, bytes, report);
    }

    private byte[] generatePdfBytes(PdfTarget target) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            document.getDocument().setVersion(target.expectedPdfVersion());
            document.setDocumentInformation(buildInfo(target));

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                stream.newLineAtOffset(50, 770);
                stream.showText("PDF Profile Example");
                stream.endText();

                stream.beginText();
                stream.setFont(PDType1Font.HELVETICA, 12);
                stream.newLineAtOffset(50, 730);
                stream.showText("Target profile: " + target.displayName());
                stream.newLineAtOffset(0, -20);
                stream.showText("Family: " + target.family());
                stream.newLineAtOffset(0, -20);
                stream.showText("Expected PDF version: " + String.format(Locale.US, "%.1f", target.expectedPdfVersion()));
                stream.newLineAtOffset(0, -20);
                stream.showText("Generated on: " + LocalDate.now());
                stream.newLineAtOffset(0, -20);
                stream.showText(profileDescription(target));
                stream.endText();
            }

            document.save(out);
            return out.toByteArray();
        }
    }

    private PDDocumentInformation buildInfo(PdfTarget target) {
        PDDocumentInformation info = new PDDocumentInformation();
        info.setTitle("Profile sample " + target.displayName());
        info.setAuthor("docCreator");
        info.setSubject("Generated profile sample for " + target.displayName());
        info.setKeywords("pdf,profile," + target.slug());
        info.setCreator("docCreator Spring Boot service");
        info.setProducer("PDFBox");
        return info;
    }

    private String profileDescription(PdfTarget target) {
        return switch (target.family()) {
            case "PDF_A" -> "Includes archival-profile marker and self-contained content intent.";
            case "PDF_X" -> "Includes print-exchange marker and output-intent placeholder.";
            case "PDF_UA" -> "Includes accessibility marker and structure intent placeholder.";
            case "PDF_VT" -> "Includes variable-print marker for transactional workflows.";
            default -> "Version sample with minimal content and explicit version header.";
        };
    }
}
