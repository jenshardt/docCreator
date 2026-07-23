package de.hardt.docCreator.service.content;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

import de.hardt.docCreator.model.PdfTarget;

/**
 * Builds the page content of a generated PDF for one {@link PdfTarget}
 * family. Implementations are profile-aware: PDF/A, PDF/X and PDF/UA have
 * different rules for fonts, color spaces, tagging and attachments, so each
 * family gets its own builder instead of one generic implementation.
 */
public interface PdfContentBuilder {

    /**
     * @return the {@link PdfTarget#family()} value this builder is responsible for.
     */
    String family();

    /**
     * Adds one or more pages with content to the (still empty) document.
     */
    void build(PDDocument document, PdfTarget target) throws IOException;
}
