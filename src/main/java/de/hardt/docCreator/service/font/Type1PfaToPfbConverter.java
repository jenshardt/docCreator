package de.hardt.docCreator.service.font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Converts a "raw"/PFA-style Adobe Type 1 font program (cleartext ASCII
 * header, followed directly by the eexec-encrypted binary charstring data,
 * followed by the 512-zero/cleartomark ASCII trailer - the layout used by
 * e.g. the URW++ Base 35 {@code .t1} font files) into the PFB (Printer Font
 * Binary) container format required by
 * {@link org.apache.pdfbox.pdmodel.font.PDType1Font}, whose
 * {@link org.apache.fontbox.pfb.PfbParser} insists on genuine
 * {@code 0x80}-prefixed PFB segment headers.
 *
 * <p>This is a purely mechanical re-wrapping of the exact same font program
 * bytes into three length-prefixed segments (ASCII / binary / ASCII); no
 * font data is altered, generated or reinterpreted.</p>
 */
final class Type1PfaToPfbConverter {

    private static final byte[] EEXEC = "eexec".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] CLEARTOMARK = "cleartomark".getBytes(StandardCharsets.US_ASCII);

    private static final int PFB_START_MARKER = 0x80;
    private static final int PFB_ASCII_MARKER = 0x01;
    private static final int PFB_BINARY_MARKER = 0x02;
    private static final int PFB_EOF_MARKER = 0x03;

    private Type1PfaToPfbConverter() {
    }

    /**
     * Converts raw/PFA Type 1 font bytes into PFB format.
     *
     * @param raw the raw font program bytes (ASCII header + binary eexec
     *            data + ASCII zero/cleartomark trailer, no PFB segmentation)
     * @return the same font program, re-wrapped as PFB
     * @throws IOException if the input does not look like a Type 1 font
     *                      (missing {@code eexec} or {@code cleartomark})
     */
    static byte[] convert(byte[] raw) throws IOException {
        int eexecIndex = indexOf(raw, EEXEC, 0);
        if (eexecIndex < 0) {
            throw new IOException("Not a recognizable Type 1 font: 'eexec' keyword not found");
        }
        int segment1End = eexecIndex + EEXEC.length;
        // Include exactly one line terminator (CR, LF or CRLF) after "eexec"
        // in the ASCII header segment; the encrypted binary data starts
        // right after it.
        if (segment1End < raw.length && raw[segment1End] == '\r') {
            segment1End++;
        }
        if (segment1End < raw.length && raw[segment1End] == '\n') {
            segment1End++;
        }

        int cleartomarkIndex = lastIndexOf(raw, CLEARTOMARK, raw.length - CLEARTOMARK.length);
        if (cleartomarkIndex < 0) {
            throw new IOException("Not a recognizable Type 1 font: 'cleartomark' trailer not found");
        }
        // Walk back over the ASCII zero-padding/line-ending trailer (512
        // zeros, conventionally in lines of 64, separated by CR, LF or
        // CRLF) that precedes "cleartomark" to find where the binary
        // segment ends.
        int segment2End = cleartomarkIndex;
        while (segment2End > segment1End
                && (raw[segment2End - 1] == '0' || raw[segment2End - 1] == '\r' || raw[segment2End - 1] == '\n')) {
            segment2End--;
        }

        byte[] segment1 = Arrays.copyOfRange(raw, 0, segment1End);
        byte[] segment2 = Arrays.copyOfRange(raw, segment1End, segment2End);
        byte[] segment3 = Arrays.copyOfRange(raw, segment2End, raw.length);

        ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length + 32);
        writeSegment(out, PFB_ASCII_MARKER, segment1);
        writeSegment(out, PFB_BINARY_MARKER, segment2);
        writeSegment(out, PFB_ASCII_MARKER, segment3);
        out.write(PFB_START_MARKER);
        out.write(PFB_EOF_MARKER);
        return out.toByteArray();
    }

    private static void writeSegment(ByteArrayOutputStream out, int marker, byte[] data) {
        out.write(PFB_START_MARKER);
        out.write(marker);
        int length = data.length;
        out.write(length & 0xFF);
        out.write((length >>> 8) & 0xFF);
        out.write((length >>> 16) & 0xFF);
        out.write((length >>> 24) & 0xFF);
        out.write(data, 0, data.length);
    }

    private static int indexOf(byte[] data, byte[] pattern, int fromIndex) {
        outer:
        for (int i = fromIndex; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static int lastIndexOf(byte[] data, byte[] pattern, int fromIndex) {
        outer:
        for (int i = Math.min(fromIndex, data.length - pattern.length); i >= 0; i--) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
