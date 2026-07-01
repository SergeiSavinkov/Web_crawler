package crawler.logging;

import crawler.models.PageResult;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

public class TxtReporter implements Closeable {

    private final BufferedWriter writer;
    private int counter = 0;

    public TxtReporter(String fileName) {
        try {
            writer = new BufferedWriter(new FileWriter(fileName), 16384);
        } catch (IOException e) {
            throw new RuntimeException("Can't open report file: " + fileName, e);
        }
    }

    public synchronized void report(PageResult result, String parentUrl) {
        try {

            String line;

            if (result.hasError()) {
                line = "Page (url='" + result.getUrl() +
                        "', parentUrl='" + parentUrl +
                        "') was fetched with the following error: " +
                        result.getErrorMessage() + ".\n";
            } else {
                line = "Page (url='" + result.getUrl() +
                        "', parentUrl='" + parentUrl +
                        "') got successfully parsed.\n";
            }

            writer.write(line);

            counter++;
            if (counter % 1000 == 0) {
                writer.flush();
            }

        } catch (IOException exception) {
            System.err.println("An error occurred during writing the report: " + exception.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException exception) {
            System.err.println("Failed to close report writer: " + exception.getMessage());
        }
    }
}