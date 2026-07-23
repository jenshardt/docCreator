package de.hardt.docCreator.service.image;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;

class PdfImageServiceTest {

    private final PdfImageService imageService = new PdfImageService();

    @Test
    void createsNonEmptyRgbSampleImage() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDImageXObject image = imageService.createSampleImage(document);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(400);
            assertThat(image.getHeight()).isEqualTo(220);
            assertThat(image.getImage()).isNotNull();
        }
    }
}
