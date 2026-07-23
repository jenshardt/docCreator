package de.hardt.docCreator.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.springframework.stereotype.Service;

import de.hardt.docCreator.model.GeneratedPdfResult;
import de.hardt.docCreator.model.PdfTarget;
import de.hardt.docCreator.model.ValidationReport;
import de.hardt.docCreator.service.content.PdfContentBuilder;

@Service
public class PdfProfileService {

    private final PdfValidationService validationService;
    private final Map<String, PdfContentBuilder> contentBuildersByFamily;

    public PdfProfileService(PdfValidationService validationService, List<PdfContentBuilder> contentBuilders) {
        this.validationService = validationService;
        this.contentBuildersByFamily = contentBuilders.stream()
                .collect(Collectors.toMap(PdfContentBuilder::family, Function.identity()));
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

            PdfContentBuilder contentBuilder = contentBuildersByFamily.get(target.family());
            if (contentBuilder == null) {
                throw new IllegalStateException("No PdfContentBuilder registered for family " + target.family());
            }
            contentBuilder.build(document, target);

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
}
