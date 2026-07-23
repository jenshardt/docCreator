package de.hardt.docCreator.service.content;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * Small helper that manages page breaks and a vertical text cursor across a
 * multi-page document, so individual content sections do not have to track
 * {@link PDPageContentStream} lifecycles or Y-offsets themselves.
 *
 * <p>Kept intentionally simple (no word-wrap, no rich layout): its only job
 * is to let {@link AbstractPdfContentBuilder} lay out several distinct
 * sections (cover, font demo, image demo, ...) as separate pages without
 * repeating page/stream bookkeeping in every subclass.
 */
public class PdfLayoutBuilder implements AutoCloseable {

    private static final float MARGIN_LEFT = 50;
    private static final float MARGIN_TOP = 792;
    private static final float MARGIN_BOTTOM = 50;
    private static final float DEFAULT_LINE_HEIGHT = 16;

    private final PDDocument document;
    private PDPageContentStream stream;
    private PDPage page;
    private float cursorY;

    public PdfLayoutBuilder(PDDocument document) throws IOException {
        this.document = document;
        newPage();
    }

    /** Starts a new page, closing the content stream of the previous one. */
    public void newPage() throws IOException {
        closeStream();
        page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        stream = new PDPageContentStream(document, page);
        cursorY = MARGIN_TOP;
    }

    /** Starts a new page unless the current one already has enough room left. */
    public void ensureSpace(float requiredHeight) throws IOException {
        if (cursorY - requiredHeight < MARGIN_BOTTOM) {
            newPage();
        }
    }

    public void heading(String value) throws IOException {
        ensureSpace(DEFAULT_LINE_HEIGHT * 2);
        line(PDType1Font.HELVETICA_BOLD, 16, value);
        moveDown(6);
    }

    public void line(String value) throws IOException {
        line(PDType1Font.HELVETICA, 12, value);
    }

    public void line(PDFont font, float size, String value) throws IOException {
        ensureSpace(DEFAULT_LINE_HEIGHT);
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(MARGIN_LEFT, cursorY);
        stream.showText(value);
        stream.endText();
        cursorY -= DEFAULT_LINE_HEIGHT;
    }

    public void drawImage(PDImageXObject image, float width, float height) throws IOException {
        ensureSpace(height + DEFAULT_LINE_HEIGHT);
        stream.drawImage(image, MARGIN_LEFT, cursorY - height, width, height);
        cursorY -= (height + DEFAULT_LINE_HEIGHT);
    }

    public void moveDown(float amount) {
        cursorY -= amount;
    }

    public float cursorY() {
        return cursorY;
    }

    public float marginLeft() {
        return MARGIN_LEFT;
    }

    public PDPageContentStream stream() {
        return stream;
    }

    public PDDocument document() {
        return document;
    }

    public PDPage currentPage() {
        return page;
    }

    private void closeStream() throws IOException {
        if (stream != null) {
            stream.close();
        }
    }

    @Override
    public void close() throws IOException {
        closeStream();
    }
}
