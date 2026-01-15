package com.somshare.somshare.exception;

public class AlreadyDownloadedException extends RuntimeException {

    private final String pdfUrl;
    private final String fileName;

    public AlreadyDownloadedException(String message, String pdfUrl, String fileName) {
        super(message);
        this.pdfUrl = pdfUrl;
        this.fileName = fileName;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public String getFileName() {
        return fileName;
    }
}
