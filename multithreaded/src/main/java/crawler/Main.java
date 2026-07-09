package crawler;

import crawler.services.Crawler;
import crawler.services.impl.ParallelCrawlerImpl;

// cd multithreaded
// Assembly and Execution: mvn clean compile exec:java
public class Main {
    public static void main(String[] args) {
        String url = "https://famnit.upr.si";

        Crawler crawler = new ParallelCrawlerImpl(url);
        long start = System.currentTimeMillis();

        int pages = crawler.crawl();

        long end = System.currentTimeMillis();

        System.out.println("Visited pages: " + pages);
        System.out.println("Time: " + (end - start) + " ms");
    }
}