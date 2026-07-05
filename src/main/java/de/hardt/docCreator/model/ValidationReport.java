package de.hardt.docCreator.model;

import java.util.List;

public record ValidationReport(String target, boolean valid, List<ValidationCheck> checks) {

    public String summarize() {
        long failed = checks.stream().filter(check -> !check.passed()).count();
        if (failed == 0) {
            return "All checks passed";
        }
        return failed + " check(s) failed";
    }
}
