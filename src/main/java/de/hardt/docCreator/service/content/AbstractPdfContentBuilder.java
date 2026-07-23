package de.hardt.docCreator.service.content;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import de.hardt.docCreator.model.PdfTarget;
import de.hardt.docCreator.service.font.FontAsset;
import de.hardt.docCreator.service.font.PdfFontService;
import de.hardt.docCreator.service.image.PdfImageService;

/**
 * Shared base for the family-specific content builders. Phase 0 reproduced
 * the previous single-page/text-only content unchanged. Phase 1 (this
 * class) introduces the multi-page skeleton (cover, font demo, image demo,
 * table/vector graphics, interactive elements) that later phases fill in
 * with real embedded fonts (Phase 2), profile-specific image color spaces
 * (Phase 3) and real hyperlinks/bookmarks/form fields/attachments (Phase 4),
 * without needing to touch the other families.
 */
public abstract class AbstractPdfContentBuilder implements PdfContentBuilder {

    protected final PdfFontService fontService;
    protected final PdfImageService imageService;

    protected AbstractPdfContentBuilder(PdfFontService fontService, PdfImageService imageService) {
        this.fontService = fontService;
        this.imageService = imageService;
    }

    @Override
    public void build(PDDocument document, PdfTarget target) throws IOException {
        try (PdfLayoutBuilder layout = new PdfLayoutBuilder(document)) {
            buildCoverPage(layout, target);
            layout.newPage();
            buildFontDemoPage(layout, target);
            layout.newPage();
            buildImageDemoPage(layout, target);
            layout.newPage();
            buildTableAndVectorGraphicsPage(layout, target);
            layout.newPage();
            buildInteractiveElementsPage(layout, target);
        }
    }

    /**
     * Cover page. Keeps the exact original title/metadata block (same text,
     * font and position) that existing validation and README examples rely
     * on, in particular the literal "Target profile: &lt;name&gt;" line.
     */
    private void buildCoverPage(PdfLayoutBuilder layout, PdfTarget target) throws IOException {
        PDPageContentStream stream = layout.stream();
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
        stream.showText(profileDescription());
        stream.newLineAtOffset(0, -20);
        stream.showText("This document also demonstrates fonts, images and further elements (see following pages).");
        stream.endText();
    }

    /**
     * Font demonstration section: embeds Liberation Sans/Serif/Mono in all
     * four styles, a genuine Type 1/PostScript font (URW Nimbus Sans), a
     * Cyrillic sample (Noto Sans) and a CJK sample (Noto Sans SC, subsetted
     * to the exact glyphs used below). All fonts are embedded as subsets
     * except the Type 1 one (PDFBox has no subsetting support for simple
     * Type 1 fonts, so it is always embedded in full).
     */
    protected void buildFontDemoPage(PdfLayoutBuilder layout, PdfTarget target) throws IOException {
        PDDocument document = layout.document();
        String sample = "The quick brown fox jumps over the lazy dog";

        layout.heading("Font Demonstration");
        layout.line("Embedded font families, styles and scripts:");
        layout.moveDown(4);

        layout.line("Sans-serif (Liberation Sans):");
        layout.line(fontService.embed(document, FontAsset.SANS_REGULAR), 12, sample + " (regular)");
        layout.line(fontService.embed(document, FontAsset.SANS_BOLD), 12, sample + " (bold)");
        layout.line(fontService.embed(document, FontAsset.SANS_ITALIC), 12, sample + " (italic)");
        layout.line(fontService.embed(document, FontAsset.SANS_BOLD_ITALIC), 12, sample + " (bold italic)");
        layout.moveDown(4);

        layout.line("Serif (Liberation Serif):");
        layout.line(fontService.embed(document, FontAsset.SERIF_REGULAR), 12, sample + " (regular)");
        layout.line(fontService.embed(document, FontAsset.SERIF_BOLD), 12, sample + " (bold)");
        layout.line(fontService.embed(document, FontAsset.SERIF_ITALIC), 12, sample + " (italic)");
        layout.line(fontService.embed(document, FontAsset.SERIF_BOLD_ITALIC), 12, sample + " (bold italic)");
        layout.moveDown(4);

        layout.line("Monospace (Liberation Mono):");
        layout.line(fontService.embed(document, FontAsset.MONO_REGULAR), 12, sample + " (regular)");
        layout.line(fontService.embed(document, FontAsset.MONO_BOLD), 12, sample + " (bold)");
        layout.line(fontService.embed(document, FontAsset.MONO_ITALIC), 12, sample + " (italic)");
        layout.line(fontService.embed(document, FontAsset.MONO_BOLD_ITALIC), 12, sample + " (bold italic)");
        layout.moveDown(4);

        layout.line("Type 1 / PostScript (URW Nimbus Sans):");
        layout.line(fontService.embed(document, FontAsset.SANS_TYPE1), 12, sample);
        layout.moveDown(4);

        layout.line("Cyrillic (Noto Sans):");
        layout.line(fontService.embed(document, FontAsset.CYRILLIC_SANS), 12,
                "\u041F\u0440\u0438\u0432\u0435\u0442, \u043C\u0438\u0440");
        layout.moveDown(4);

        layout.line("Chinese / CJK (Noto Sans SC, subset):");
        var cjkFont = fontService.embed(document, FontAsset.CJK_SANS);
        layout.line(cjkFont, 12, "\u4F60\u597D, \u4E16\u754C");
        layout.line(cjkFont, 12, "\u8FD9\u662F\u4E00\u4E2A\u6D4B\u8BD5\u6587\u6863\u3002");
    }

    /**
     * Image demonstration section. Already uses the real runtime-generated
     * sample image (Phase 0 infrastructure); Phase 3 adds profile-specific
     * color space handling (sRGB ICC for PDF/A, CMYK ICC output intent for
     * PDF/X, no alpha for PDF/A-1).
     */
    protected void buildImageDemoPage(PdfLayoutBuilder layout, PdfTarget target) throws IOException {
        layout.heading("Image Demonstration");
        layout.line("A runtime-generated raster image (no static image assets, no licensing concerns):");
        layout.moveDown(6);
        PDImageXObject image = imageService.createSampleImage(layout.document());
        layout.drawImage(image, 300, 165);
    }

    /**
     * Structural placeholder for tables and vector graphics. Phase 4 extends
     * this with a proper table-rendering utility and richer vector shapes.
     */
    protected void buildTableAndVectorGraphicsPage(PdfLayoutBuilder layout, PdfTarget target) throws IOException {
        layout.heading("Tables & Vector Graphics");
        layout.line("A simple table and vector graphics will be added on this page in Phase 4.");
    }

    /**
     * Structural placeholder for hyperlinks, bookmarks, form fields and
     * attachments. Phase 4 implements the real elements, respecting
     * {@link PdfTarget#supportsAttachments()} for the attachment part.
     */
    protected void buildInteractiveElementsPage(PdfLayoutBuilder layout, PdfTarget target) throws IOException {
        layout.heading("Interactive Elements");
        layout.line("Hyperlinks, bookmarks and form fields will be added on this page in Phase 4.");
        layout.line("Attachments for this target: " + (target.supportsAttachments() ? "allowed" : "not allowed"));
    }

    protected abstract String profileDescription();
}

