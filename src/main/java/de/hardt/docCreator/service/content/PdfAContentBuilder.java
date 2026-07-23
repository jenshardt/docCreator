package de.hardt.docCreator.service.content;

import org.springframework.stereotype.Component;

import de.hardt.docCreator.service.font.PdfFontService;
import de.hardt.docCreator.service.image.PdfImageService;

@Component
public class PdfAContentBuilder extends AbstractPdfContentBuilder {

    public PdfAContentBuilder(PdfFontService fontService, PdfImageService imageService) {
        super(fontService, imageService);
    }

    @Override
    public String family() {
        return "PDF_A";
    }

    @Override
    protected String profileDescription() {
        return "Includes archival-profile marker and self-contained content intent.";
    }
}
