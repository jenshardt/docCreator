package de.hardt.docCreator.service.content;

import org.springframework.stereotype.Component;

import de.hardt.docCreator.service.font.PdfFontService;
import de.hardt.docCreator.service.image.PdfImageService;

@Component
public class VersionPdfContentBuilder extends AbstractPdfContentBuilder {

    public VersionPdfContentBuilder(PdfFontService fontService, PdfImageService imageService) {
        super(fontService, imageService);
    }

    @Override
    public String family() {
        return "PDF_VERSION";
    }

    @Override
    protected String profileDescription() {
        return "Version sample with minimal content and explicit version header.";
    }
}
