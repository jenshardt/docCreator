package de.hardt.docCreator.service.content;

import org.springframework.stereotype.Component;

import de.hardt.docCreator.service.font.PdfFontService;
import de.hardt.docCreator.service.image.PdfImageService;

@Component
public class PdfXContentBuilder extends AbstractPdfContentBuilder {

    public PdfXContentBuilder(PdfFontService fontService, PdfImageService imageService) {
        super(fontService, imageService);
    }

    @Override
    public String family() {
        return "PDF_X";
    }

    @Override
    protected String profileDescription() {
        return "Includes print-exchange marker and output-intent placeholder.";
    }
}
