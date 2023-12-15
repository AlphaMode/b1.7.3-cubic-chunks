package me.alphamode;

import net.minecraft.util.Crash;

public class ReportedException extends RuntimeException {
    private final Crash report;

    public ReportedException(Crash crashReport) {
        this.report = crashReport;
    }

    public Crash getReport() {
        return this.report;
    }

    public Throwable getCause() {
        return this.report.exception;
    }

    public String getMessage() {
        return this.report.crashTitle;
    }
}