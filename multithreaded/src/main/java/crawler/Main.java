package crawler;

import crawler.services.Crawler;
import crawler.services.impl.ParallelCrawlerImpl;

// cd sequential
// Assembly: mvn clean compile
// Execution: mvn exec:java
public class Main {
    public static void main(String[] args) {
        String url = "https://famnit.upr.si"; // could be received from args

        Crawler crawler = new ParallelCrawlerImpl(url);
        int numberOfVisitedPages = crawler.crawl();
        System.out.printf("Number of Visited Pages: %d%n", numberOfVisitedPages);
        long start = System.currentTimeMillis();

        int pages = crawler.crawl();

        long end = System.currentTimeMillis();

        System.out.println("Visited pages: " + pages);
        System.out.println("Time: " + (end - start) + " ms");
    }
}