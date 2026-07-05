package crawler.utils;

import crawler.models.PageResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class HttpUtils {

    private static final String LINKS_SELECTOR = "a[href]";

    private static final String HREF_ATTRIBUTE_SELECTOR = "abs:href";

    private static final String GET_REQUEST_HTTP_METHOD = "GET";

    private static final String HTML_CONTENT_TYPE = "text/html";

    private static final int DEFAULT_TIMEOUT = 5000;

    public static List<String> extractUrlLinksBy(Document document) {
        return Optional.ofNullable(document)
                .map(doc -> doc.select(LINKS_SELECTOR))
                .stream()
                .flatMap(Collection::stream)
                .map(element -> element.attr(HREF_ATTRIBUTE_SELECTOR))
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.toList());
    }

    public static String fetchBodyAsString(String urlString) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = URI.create(urlString).toURL();

            connection = (HttpURLConnection) url.openConnection();
            configureConnection(connection);
            connection.connect();
            validateConnectionResponse(connection);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder responseBodyStringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBodyStringBuilder.append(line).append(System.lineSeparator());
                }
                return responseBodyStringBuilder.toString().trim();
            }
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static void configureConnection(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod(GET_REQUEST_HTTP_METHOD);
        connection.setConnectTimeout(DEFAULT_TIMEOUT);
        connection.setReadTimeout(DEFAULT_TIMEOUT);
        connection.setInstanceFollowRedirects(true);
    }

    private static void validateConnectionResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        String responseContentType = connection.getContentType();

        if (responseCode != HttpURLConnection.HTTP_OK)
            throw new IOException("Unexpected response code returned, expected '%s' got '%s'"
                    .formatted(HttpURLConnection.HTTP_OK,  responseCode));

        if (responseContentType == null || !responseContentType.trim().contains(HTML_CONTENT_TYPE))
            throw new IOException("Unexpected response content type returned, expected '%s' got '%s'"
                    .formatted(HTML_CONTENT_TYPE,  responseCode));
    }

    public static Document getDocumentBy(String urlString) throws IOException {
        String htmlBody = fetchBodyAsString(urlString);
        return Jsoup.parse(htmlBody, urlString);
    }

    public static PageResult getPageResultBy(String urlString) {
        try {
            return PageResult.of(urlString, getDocumentBy(urlString));
        } catch (IOException exception) {
            return PageResult.of(urlString, exception);
        }
    }
}
