package crawler.services.impl;

import crawler.distributed.MpjTags;
import crawler.filters.DomainFilter;
import crawler.logging.TxtReporter;
import crawler.models.CrawlResult;
import crawler.models.PageResult;
import crawler.services.Crawler;
import crawler.utils.HttpUtils;
import mpi.MPI;
import mpi.Status;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MpjDistributedCrawlerImpl implements Crawler {
    private static final String STOP_TASK = "__STOP_CRAWLER_WORKER__";

    private final Queue<String> queue = new LinkedList<>();
    private final Set<String> visited = new HashSet<>();
    private final Set<String> seen = new HashSet<>();
    private final Map<String, String> urlToParentUrlMap = new HashMap<>();

    private final DomainFilter domainFilter;
    private final String reporterFilename;
    private final int processCount;

    public MpjDistributedCrawlerImpl(String url, String reportFileName, int processCount) {
        this.domainFilter = new DomainFilter(url);
        this.reporterFilename = reportFileName;
        this.processCount = processCount;
        this.queue.add(url);
        this.seen.add(url);
        this.urlToParentUrlMap.put(url, null);
    }

    @Override
    public int crawl() {
        if (processCount < 2) {
            throw new IllegalStateException("MPJ distributed crawler needs at least 2 processes.");
        }

        long startTime = System.nanoTime();
        int activeWorkers = 0;
        boolean[] workerIsActive = new boolean[processCount];

        try (TxtReporter reporter = new TxtReporter(reporterFilename)) {


            while (!queue.isEmpty() || activeWorkers > 0) {

                for (int workerRank = 1; workerRank < processCount && !queue.isEmpty(); workerRank++) {
                    if (!workerIsActive[workerRank]) {
                        sendNextTask(workerRank);
                        workerIsActive[workerRank] = true;
                        activeWorkers++;
                    }
                }

                if (activeWorkers == 0) {
                    break;
                }

                Object[] buffer = new Object[1];

                Status status = MPI.COMM_WORLD.Recv(buffer, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, MpjTags.RESULT);

                int workerRank = status.source;
                CrawlResult result = (CrawlResult) buffer[0];

                workerIsActive[workerRank] = false;
                activeWorkers--;

                visited.add(result.getUrl());
                reporter.report(result, urlToParentUrlMap.get(result.getUrl()));


                // statistics
                int processedPages = visited.size();

                if (processedPages % 500 == 0) {
                    double elapsed = (System.nanoTime() - startTime) / 1_000_000_000.0;
                    double speed = processedPages / elapsed;
                    System.out.printf("Processed %d pages in %.2f seconds, speed: %.2f pages/s%n".formatted(processedPages, elapsed, speed));
                }

                if (!result.hasError()) {
                    for (String link : result.getLinks()) {
                        if (!domainFilter.accepts(link)) {
                            continue;
                        }

                        if (seen.add(link)) {
                            queue.add(link);
                            urlToParentUrlMap.put(link, result.getUrl());
                        }
                    }
                }
            }

            for (int workerRank = 1; workerRank < processCount; workerRank++) {
                sendStopTask(workerRank);
            }
        }

        return visited.size();
    }

    public static void runWorker() {
        while (true) {
            String url = receiveTask();

            if (STOP_TASK.equals(url)) {
                return;
            }

            CrawlResult result = crawlUrl(url);
            sendResult(result);
        }
    }

    private static CrawlResult crawlUrl(String url) {
        PageResult page = HttpUtils.getPageResultBy(url);

        if (page.hasError()) {
            return CrawlResult.error(url, page.getErrorMessage());
        }

        return CrawlResult.success(url, HttpUtils.extractUrlLinksBy(page.getDocument()));
    }

    private void sendNextTask(int workerRank) {
        String nextUrl = queue.poll();
        sendTask(workerRank, nextUrl);
    }

    private static String receiveTask() {
        Object[] buffer = new Object[1];
        MPI.COMM_WORLD.Recv(buffer, 0, 1, MPI.OBJECT, 0, MpjTags.TASK);
        return (String) buffer[0];
    }

    private static void sendTask(int workerRank, String url) {
        Object[] buffer = new Object[]{url};
        MPI.COMM_WORLD.Send(buffer, 0, 1, MPI.OBJECT, workerRank, MpjTags.TASK);
    }

    private static void sendStopTask(int workerRank) {
        sendTask(workerRank, STOP_TASK);
    }

    private static void sendResult(CrawlResult result) {
        Object[] buffer = new Object[]{result};
        MPI.COMM_WORLD.Send(buffer, 0, 1, MPI.OBJECT, 0, MpjTags.RESULT);
    }
}
