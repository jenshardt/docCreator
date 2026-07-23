package de.hardt.docCreator.service.content;

import org.springframework.stereotype.Component;

import de.hardt.docCreator.service.font.PdfFontService;
import de.hardt.docCreator.service.image.PdfImageService;

@Component
public class PdfUaContentBuilder extends AbstractPdfContentBuilder {

    public PdfUaContentBuilder(PdfFontService fontService, PdfImageService imageService) {
        super(fontService, imageService);
    }

    @Override
    public String family() {
        return "PDF_UA";
    }

    @Override
    protected String profileDescription() {
        return "Includes accessibility marker and structure intent placeholder.";
    }
}
