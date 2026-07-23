package de.hardt.docCreator.service.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

/**
 * Generates raster images at runtime (via Java2D) instead of bundling static
 * image assets, avoiding any image-licensing questions.
 */
@Service
public class PdfImageService {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 220;

    public PDImageXObject createSampleImage(PDDocument document) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);

            graphics.setColor(new Color(70, 130, 180));
            int[] barHeights = { 60, 120, 90, 150, 70 };
            int barWidth = 40;
            int gap = 20;
            int x = 20;
            for (int barHeight : barHeights) {
                graphics.fillRect(x, HEIGHT - 20 - barHeight, barWidth, barHeight);
                x += barWidth + gap;
            }

            graphics.setColor(Color.DARK_GRAY);
            graphics.setStroke(new BasicStroke(2f));
            graphics.drawRect(5, 5, WIDTH - 10, HEIGHT - 10);

            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            graphics.drawString("Generated sample image", 20, 20);
        } finally {
            graphics.dispose();
        }
        return LosslessFactory.createFromImage(document, image);
    }
}
