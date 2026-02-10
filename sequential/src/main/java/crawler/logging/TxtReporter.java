package crawler.logging;

import crawler.models.PageResult;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

public class TxtReporter implements Closeable {
    private final BufferedWriter writer;

    public TxtReporter(String fileName) {
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Can't open report file: " + fileName, e);
        }
    }

    public void report(PageResult result, String parentUrl) {
        try {
            if (result.hasError()) {
                writer.write("Page (url='%s', parentUrl='%s') was fetched with the following error: %s.%n".formatted(
                        result.getUrl(),
                        parentUrl,
                        result.getErrorMessage()));
            } else {
                writer.write("Page (url='%s', parentUrl='%s') got successfully parsed.%n".formatted(
                        result.getUrl(), parentUrl));
            }
            writer.flush();
        } catch (IOException exception) {
            System.err.println("An error occurred during writing the report: " + exception.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException exception) {
            System.err.println("Failed to close report writer: " + exception.getMessage());
        }
    }
}
