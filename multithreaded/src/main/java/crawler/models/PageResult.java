package crawler.models;

import org.jsoup.nodes.Document;

public class PageResult {

    private final String url;
    private final Document document;
    private final Exception exception;

    private PageResult(String url, Document document, Exception exception) {
        this.url = url;
        this.document = document;
        this.exception = exception;
    }

    public static PageResult of(String url, Document document) {
        return new PageResult(url, document, null);
    }

    public static PageResult of(String url, Exception exception) {
        return new PageResult(url, null, exception);
    }

    public Document getDocument() {
        return document;
    }

    public String getErrorMessage() {
        return exception.getMessage();
    }

    public boolean hasError() {
        return exception != null;
    }

    public String getUrl() {
        return url;
    }
}
