package de.hardt.docCreator.service.font;

/**
 * Embeddable font resources bundled with the application (classpath
 * resources under {@code fonts/}). Most fonts are free/open-source (OFL or
 * Apache licensed) and are embedded as subsets by {@link PdfFontService}.
 * The Type 1 font is AGPL-3.0 licensed with an explicit font-embedding
 * exception (see {@code fonts/urw-nimbus/LICENSE.txt}) permitting use in
 * generated PDF documents regardless of the document's own license; it is
 * always embedded in full (PDFBox has no subsetting support for simple
 * Type 1 fonts).
 */
public enum FontAsset {

    SANS_REGULAR("fonts/liberation/LiberationSans-Regular.ttf", FontFormat.TRUE_TYPE),
    SANS_BOLD("fonts/liberation/LiberationSans-Bold.ttf", FontFormat.TRUE_TYPE),
    SANS_ITALIC("fonts/liberation/LiberationSans-Italic.ttf", FontFormat.TRUE_TYPE),
    SANS_BOLD_ITALIC("fonts/liberation/LiberationSans-BoldItalic.ttf", FontFormat.TRUE_TYPE),

    SERIF_REGULAR("fonts/liberation/LiberationSerif-Regular.ttf", FontFormat.TRUE_TYPE),
    SERIF_BOLD("fonts/liberation/LiberationSerif-Bold.ttf", FontFormat.TRUE_TYPE),
    SERIF_ITALIC("fonts/liberation/LiberationSerif-Italic.ttf", FontFormat.TRUE_TYPE),
    SERIF_BOLD_ITALIC("fonts/liberation/LiberationSerif-BoldItalic.ttf", FontFormat.TRUE_TYPE),

    MONO_REGULAR("fonts/liberation/LiberationMono-Regular.ttf", FontFormat.TRUE_TYPE),
    MONO_BOLD("fonts/liberation/LiberationMono-Bold.ttf", FontFormat.TRUE_TYPE),
    MONO_ITALIC("fonts/liberation/LiberationMono-Italic.ttf", FontFormat.TRUE_TYPE),
    MONO_BOLD_ITALIC("fonts/liberation/LiberationMono-BoldItalic.ttf", FontFormat.TRUE_TYPE),

    /** URW Nimbus Sans (metric-compatible clone of Helvetica), a genuine Adobe Type 1 (PostScript) font program. */
    SANS_TYPE1("fonts/urw-nimbus/NimbusSans-Regular.t1", FontFormat.TYPE1),

    /** Noto Sans, covers Latin, Cyrillic and Greek in a single font file. */
    CYRILLIC_SANS("fonts/noto/NotoSans-Regular.ttf", FontFormat.TRUE_TYPE),

    /** Reduced/subsetted Noto Sans SC sample (only demo glyphs) to keep repository size small. */
    CJK_SANS("fonts/noto/NotoSansSC-Subset.ttf", FontFormat.TRUE_TYPE);

    private final String resourcePath;
    private final FontFormat format;

    FontAsset(String resourcePath, FontFormat format) {
        this.resourcePath = resourcePath;
        this.format = format;
    }

    public String resourcePath() {
        return resourcePath;
    }

    public FontFormat format() {
        return format;
    }
}
