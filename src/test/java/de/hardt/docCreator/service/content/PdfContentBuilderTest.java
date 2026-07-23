package de.hardt.docCreator.service.content;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import de.hardt.docCreator.model.PdfTarget;
import de.hardt.docCreator.service.font.PdfFontService;
import de.hardt.docCreator.service.image.PdfImageService;

/**
 * Verifies the Phase 1 multi-page skeleton (cover, font demo, image demo,
 * table/vector graphics, interactive elements) produced by every
 * family-specific {@link PdfContentBuilder}.
 */
class PdfContentBuilderTest {

    private static final int EXPECTED_PAGE_COUNT = 5;

    private final PdfFontService fontService = new PdfFontService();
    private final PdfImageService imageService = new PdfImageService();

    static List<PdfTarget> allTargets() {
        return List.of(PdfTarget.values());
    }

    @ParameterizedTest
    @MethodSource("allTargets")
    void producesFivePageSkeletonWithExpectedSections(PdfTarget target) throws IOException {
        PdfContentBuilder builder = builderFor(target.family());

        try (PDDocument document = new PDDocument()) {
            PDDocumentInformation info = new PDDocumentInformation();
            info.setTitle("PDF Profile Example - " + target.displayName());
            document.setDocumentInformation(info);

            builder.build(document, target);
            assertThat(document.getNumberOfPages()).isEqualTo(EXPECTED_PAGE_COUNT);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);

            try (PDDocument reloaded = PDDocument.load(out.toByteArray())) {
                String text = new PDFTextStripper().getText(reloaded);
                assertThat(text).contains("Target profile: " + target.displayName());
                assertThat(text).contains("Family: " + target.family());
                assertThat(text).contains("Font Demonstration");
                assertThat(text).contains("The quick brown fox jumps over the lazy dog (bold italic)");
                assertThat(text).contains("Type 1 / PostScript (URW Nimbus Sans):");
                assertThat(text).contains("\u041F\u0440\u0438\u0432\u0435\u0442, \u043C\u0438\u0440");
                assertThat(text).contains("\u8FD9\u662F\u4E00\u4E2A\u6D4B\u8BD5\u6587\u6863\u3002");
                assertThat(text).contains("Image Demonstration");
                assertThat(text).contains("Tables & Vector Graphics");
                assertThat(text).contains("Interactive Elements");
                assertThat(text).contains("Attachments for this target: "
                        + (target.supportsAttachments() ? "allowed" : "not allowed"));
            }
        }
    }

    private PdfContentBuilder builderFor(String family) {
        return switch (family) {
            case "PDF_A" -> new PdfAContentBuilder(fontService, imageService);
            case "PDF_X" -> new PdfXContentBuilder(fontService, imageService);
            case "PDF_UA" -> new PdfUaContentBuilder(fontService, imageService);
            case "PDF_VT" -> new PdfVtContentBuilder(fontService, imageService);
            default -> new VersionPdfContentBuilder(fontService, imageService);
        };
    }
}
