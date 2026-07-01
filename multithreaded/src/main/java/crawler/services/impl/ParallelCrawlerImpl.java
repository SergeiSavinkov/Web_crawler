package crawler.services.impl;

import crawler.filters.DomainFilter;
import crawler.logging.TxtReporter;
import crawler.models.PageResult;
import crawler.services.Crawler;
import crawler.utils.HttpUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelCrawlerImpl implements Crawler {

    private static final String DEFAULT_REPORTER_FILENAME = "report.txt";
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 8;

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final Set<String> seen = ConcurrentHashMap.newKeySet();
    private final Map<String, String> urlToParentUrlMap = new ConcurrentHashMap<>();

    private final AtomicInteger activeTasks = new AtomicInteger(0);

    private final AtomicInteger processedPages = new AtomicInteger(0);

    private final DomainFilter domainFilter;
    private final String reporterFilename;

    public ParallelCrawlerImpl(String url) {
        this(url, DEFAULT_REPORTER_FILENAME);
    }

    public ParallelCrawlerImpl(String url, String reportFileName) {

        this.domainFilter = new DomainFilter(url);
        queue.offer(url);
        seen.add(url);
        this.reporterFilename = reportFileName;
    }

    @Override
    public int crawl() {

        long startTime = System.nanoTime();

        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
             TxtReporter reporter = new TxtReporter(reporterFilename)) {

            for (int i = 0; i < THREAD_COUNT; i++) {

                executor.submit(() -> {

                    while (true) {

                        String currentUrl;

                        try {
                            currentUrl = queue.poll(500, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }

                        if (currentUrl == null) {
                            if (activeTasks.get() == 0) {
                                return;
                            }
                            continue;
                        }

                        activeTasks.incrementAndGet();

                        try {
                            if (!visited.add(currentUrl)) {
                                continue;
                            }

                            int count = processedPages.incrementAndGet();

                            if (count % 500 == 0) {

                                double elapsed =
                                        (System.nanoTime() - startTime) / 1_000_000_000.0;

                                double speed = count / elapsed;

                                System.out.printf(
                                        "Processed %d pages in %.2f s (%.2f pages/sec)%n",
                                        count, elapsed, speed
                                );
                            }

                            PageResult page = HttpUtils.getPageResultBy(currentUrl);
                            String parentUrl = urlToParentUrlMap.get(currentUrl);
                            reporter.report(page, parentUrl);

                            if (page.hasError()) {
                                continue;
                            }

                            List<String> links = HttpUtils.extractUrlLinksBy(page.getDocument());

                            for (String link : links) {
                                if (!domainFilter.accepts(link)) continue;

                                if (seen.add(link)) {
                                    queue.offer(link);
                                    urlToParentUrlMap.put(link, currentUrl);
                                }
                            }
                        } finally {
                            activeTasks.decrementAndGet();
                        }
                    }
                });
            }

            executor.shutdown();

            try {
                if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return visited.size();
    }
}