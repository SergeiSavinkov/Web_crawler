package crawler.filters;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static crawler.constants.StringConstants.DOT;

public class DomainFilter {

    private static final String WWW_PREFIX = "www.";

    private static final String HTTP_PREFIX = "http";
    private final String host;

    public DomainFilter(String stringUrl) {
        try {
            this.host = getHostBy(stringUrl);
        } catch (Exception e) {
            throw new InvalidUrlException("Invalid url: " + stringUrl, e);
        }
    }

    public boolean accepts(String link) {
        if (link == null) {
            return false;
        }

        if (link.isEmpty()) {
            return false;
        }

        if (!link.startsWith(HTTP_PREFIX)) {
            return false;
        }

        try {
            String linkHost = getHostBy(link);
            return linkHost.equals(host) || linkHost.endsWith(DOT + host);
        } catch (Exception e) {
            return false;
        }
    }

    private static String getDomainBy(String host) {
        host = host.toLowerCase();
        if (host.startsWith(WWW_PREFIX)) {
            host = host.substring(WWW_PREFIX.length());
        }
        return host;
    }

    private static String getHostBy(String stringUrl) throws MalformedURLException {
        URL url = URI.create(stringUrl).toURL();
        return getDomainBy(url.getHost());
    }

    public static class InvalidUrlException extends RuntimeException {
        public InvalidUrlException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
