package de.hardt.docCreator.service.content;

import org.springframework.stereotype.Component;

import de.hardt.docCreator.service.font.PdfFontService;
import de.hardt.docCreator.service.image.PdfImageService;

@Component
public class PdfVtContentBuilder extends AbstractPdfContentBuilder {

    public PdfVtContentBuilder(PdfFontService fontService, PdfImageService imageService) {
        super(fontService, imageService);
    }

    @Override
    public String family() {
        return "PDF_VT";
    }

    @Override
    protected String profileDescription() {
        return "Includes variable-print marker for transactional workflows.";
    }
}
