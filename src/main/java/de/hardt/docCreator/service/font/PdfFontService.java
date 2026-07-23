package de.hardt.docCreator.service.font;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Loads bundled font resources and embeds them into a {@link PDDocument}.
 * TrueType fonts are embedded as subsets (only the glyphs actually used end
 * up in the generated PDF); Type 1 fonts are always embedded in full since
 * PDFBox has no subsetting support for simple (non-CID) Type 1 fonts.
 *
 * <p>Font bytes are cached per {@link FontAsset} since reading the classpath
 * resource is comparatively expensive; a fresh {@link PDFont} must still be
 * created per document because PDFBox ties embedded font objects to the
 * document they were loaded into.</p>
 */
@Service
public class PdfFontService {

    private final Map<FontAsset, byte[]> fontBytesCache = new ConcurrentHashMap<>();

    public PDFont embed(PDDocument document, FontAsset asset) throws IOException {
        byte[] bytes = fontBytesCache.computeIfAbsent(asset, this::loadBytes);
        if (asset.format() == FontFormat.TYPE1) {
            byte[] pfb = Type1PfaToPfbConverter.convert(bytes);
            return new PDType1Font(document, new ByteArrayInputStream(pfb));
        }
        TrueTypeFont trueTypeFont;
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            trueTypeFont = new TTFParser().parse(in);
        }
        return PDType0Font.load(document, trueTypeFont, true);
    }

    private byte[] loadBytes(FontAsset asset) {
        try (InputStream in = new ClassPathResource(asset.resourcePath()).getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            in.transferTo(buffer);
            return buffer.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load font resource " + asset.resourcePath(), ex);
        }
    }
}

