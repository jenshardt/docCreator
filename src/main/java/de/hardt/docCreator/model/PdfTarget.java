package de.hardt.docCreator.model;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public enum PdfTarget {
    V_1_0("version-1.0", "1.0", "PDF_VERSION"),
    V_1_1("version-1.1", "1.1", "PDF_VERSION"),
    V_1_2("version-1.2", "1.2", "PDF_VERSION"),
    V_1_3("version-1.3", "1.3", "PDF_VERSION"),
    V_1_4("version-1.4", "1.4", "PDF_VERSION"),
    V_1_5("version-1.5", "1.5", "PDF_VERSION"),
    V_1_6("version-1.6", "1.6", "PDF_VERSION"),
    V_1_7("version-1.7", "1.7", "PDF_VERSION"),
    V_2_0("version-2.0", "2.0", "PDF_VERSION"),

    PDF_A_1A("pdf-a-1a", "PDF/A-1a", "PDF_A"),
    PDF_A_1B("pdf-a-1b", "PDF/A-1b", "PDF_A"),
    PDF_A_2A("pdf-a-2a", "PDF/A-2a", "PDF_A"),
    PDF_A_2B("pdf-a-2b", "PDF/A-2b", "PDF_A"),
    PDF_A_2U("pdf-a-2u", "PDF/A-2u", "PDF_A"),
    PDF_A_3A("pdf-a-3a", "PDF/A-3a", "PDF_A"),
    PDF_A_3B("pdf-a-3b", "PDF/A-3b", "PDF_A"),
    PDF_A_3U("pdf-a-3u", "PDF/A-3u", "PDF_A"),
    PDF_A_4("pdf-a-4", "PDF/A-4", "PDF_A"),
    PDF_A_4E("pdf-a-4e", "PDF/A-4e", "PDF_A"),
    PDF_A_4F("pdf-a-4f", "PDF/A-4f", "PDF_A"),

    PDF_X_1A_2001("pdf-x-1a-2001", "PDF/X-1a:2001", "PDF_X"),
    PDF_X_3_2002("pdf-x-3-2002", "PDF/X-3:2002", "PDF_X"),
    PDF_X_4("pdf-x-4", "PDF/X-4", "PDF_X"),
    PDF_X_4P("pdf-x-4p", "PDF/X-4p", "PDF_X"),
    PDF_X_5G("pdf-x-5g", "PDF/X-5g", "PDF_X"),
    PDF_X_5N("pdf-x-5n", "PDF/X-5n", "PDF_X"),
    PDF_X_5PG("pdf-x-5pg", "PDF/X-5pg", "PDF_X"),

    PDF_UA_1("pdf-ua-1", "PDF/UA-1", "PDF_UA"),
    PDF_UA_2("pdf-ua-2", "PDF/UA-2", "PDF_UA"),

    PDF_VT_1("pdf-vt-1", "PDF/VT-1", "PDF_VT"),
    PDF_VT_2("pdf-vt-2", "PDF/VT-2", "PDF_VT");

    private static final Map<String, PdfTarget> BY_SLUG = new ConcurrentHashMap<>();

    static {
        for (PdfTarget value : values()) {
            BY_SLUG.put(value.slug(), value);
        }
    }

    private final String slug;
    private final String displayName;
    private final String family;

    PdfTarget(String slug, String displayName, String family) {
        this.slug = slug;
        this.displayName = displayName;
        this.family = family;
    }

    public String slug() {
        return slug;
    }

    public String displayName() {
        return displayName;
    }

    public String family() {
        return family;
    }

    public boolean isVersionTarget() {
        return "PDF_VERSION".equals(family);
    }

    public float expectedPdfVersion() {
        if (isVersionTarget()) {
            return Float.parseFloat(displayName);
        }
        if (this == PDF_A_4 || this == PDF_A_4E || this == PDF_A_4F || this == PDF_UA_2) {
            return 2.0f;
        }
        return 1.7f;
    }

    public static Optional<PdfTarget> bySlug(String slug) {
        return Optional.ofNullable(BY_SLUG.get(slug));
    }
}
