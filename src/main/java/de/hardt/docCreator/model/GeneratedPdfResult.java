package de.hardt.docCreator.model;

public record GeneratedPdfResult(String fileName, byte[] bytes, ValidationReport report) {
}
