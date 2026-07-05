package de.hardt.docCreator.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.springframework.stereotype.Service;

import de.hardt.docCreator.config.VeraPdfProperties;
import de.hardt.docCreator.model.PdfTarget;
import de.hardt.docCreator.model.ValidationCheck;

@Service
public class VeraPdfValidationService {

    private final VeraPdfProperties properties;

    public VeraPdfValidationService(VeraPdfProperties properties) {
        this.properties = properties;
    }

    public ValidationCheck validate(PdfTarget target, byte[] data) {
        if (!("PDF_A".equals(target.family()) || "PDF_UA".equals(target.family()))) {
            return new ValidationCheck("VERAPDF", "veraPDF not required for family " + target.family(), true);
        }

        if (!properties.isEnabled()) {
            return new ValidationCheck("VERAPDF", "veraPDF validation disabled by config", false);
        }

        String executable = properties.getExecutable();
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("verapdf-", ".pdf");
            Files.write(tempFile, data);

            ProcessBuilder processBuilder = new ProcessBuilder(executable, "--format", "xml", tempFile.toString());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();

            boolean compliant = isCompliant(output);
            if (compliant) {
                return new ValidationCheck("VERAPDF", "veraPDF compliant for " + target.displayName(), true);
            }

            String compact = compact(output);
            String message = "veraPDF non-compliant (exit=" + exitCode + ") " + compact;
            return new ValidationCheck("VERAPDF", message, false);
        } catch (IOException ex) {
            return new ValidationCheck("VERAPDF", "veraPDF execution failed: " + ex.getMessage(), false);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return new ValidationCheck("VERAPDF", "veraPDF execution interrupted", false);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // Best effort cleanup for temp files.
                }
            }
        }
    }

    private boolean isCompliant(String output) {
        String normalized = output.toLowerCase(Locale.ROOT);
        return normalized.contains("<iscompliant>true</iscompliant>")
                || normalized.contains("iscompliant=\"true\"")
                || normalized.contains("compliant=\"true\"");
    }

    private String compact(String output) {
        String oneLine = output.replace('\n', ' ').replace('\r', ' ').trim();
        if (oneLine.length() <= 240) {
            return oneLine;
        }
        return oneLine.substring(0, 240) + "...";
    }
}