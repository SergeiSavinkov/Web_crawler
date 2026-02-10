package crawler;

import crawler.services.impl.CrawlerImpl;

public class Main {
    public static void main(String[] args) {
        String url = "https://famnit.upr.si"; // could be received from args

        CrawlerImpl crawler = new CrawlerImpl(url);
        int numberOfVisitedPages = crawler.crawl();
        System.out.printf("Number of Visited Pages: %d%n", numberOfVisitedPages);
    }
}