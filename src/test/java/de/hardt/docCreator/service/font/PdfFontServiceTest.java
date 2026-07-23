package de.hardt.docCreator.service.font;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PdfFontServiceTest {

    private final PdfFontService fontService = new PdfFontService();

    @ParameterizedTest
    @EnumSource(FontAsset.class)
    void embedsEveryBundledFontAndKeepsDocumentReadable(FontAsset asset) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDFont font = fontService.embed(document, asset);
            assertThat(font).isNotNull();
            assertThat(font.isEmbedded()).isTrue();

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(font, 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("Test");
                stream.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            assertThat(out.toByteArray().length).isGreaterThan(0);
        }
    }

    @Test
    void cachesFontBytesAcrossMultipleDocuments() throws IOException {
        try (PDDocument first = new PDDocument(); PDDocument second = new PDDocument()) {
            PDFont firstFont = fontService.embed(first, FontAsset.SANS_REGULAR);
            PDFont secondFont = fontService.embed(second, FontAsset.SANS_REGULAR);

            assertThat(firstFont).isNotNull();
            assertThat(secondFont).isNotNull();
            // Fonts must be distinct instances per document even though the underlying bytes are cached.
            assertThat(firstFont).isNotSameAs(secondFont);
        }
    }

    @Test
    void embeddedTextCanBeExtractedAgain() throws IOException {
        // Uses explicit unicode escapes ("Привет") so the assertion does not
        // depend on the source file's on-disk encoding.
        String cyrillicText = "\u041F\u0440\u0438\u0432\u0435\u0442";
        byte[] savedDocument;
        try (PDDocument document = new PDDocument()) {
            PDFont font = fontService.embed(document, FontAsset.CYRILLIC_SANS);
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(font, 12);
                stream.newLineAtOffset(50, 700);
                stream.showText(cyrillicText);
                stream.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            savedDocument = out.toByteArray();
        }

        // Extraction (like real validation/consumers) always works on the
        // saved+reloaded PDF, at which point subsetting has been applied.
        try (PDDocument reloaded = PDDocument.load(savedDocument)) {
            String extracted = new PDFTextStripper().getText(reloaded);
            assertThat(extracted).contains(cyrillicText);
        }
    }
}

