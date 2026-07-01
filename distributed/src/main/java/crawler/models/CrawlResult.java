package crawler.models;

import java.io.Serializable;
import java.util.List;

public class CrawlResult implements Serializable {

    private final String url;
    private final List<String> links;
    private final String errorMessage;

    private CrawlResult(String url, List<String> links, String errorMessage) {
        this.url = url;
        this.links = links;
        this.errorMessage = errorMessage;
    }

    public static CrawlResult success(String url, List<String> links) {
        return new CrawlResult(url, links, null);
    }

    public static CrawlResult error(String url, String errorMessage) {
        return new CrawlResult(url, List.of(), errorMessage);
    }

    public String getUrl() {
        return url;
    }

    public List<String> getLinks() {
        return links;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return errorMessage != null;
    }
}
