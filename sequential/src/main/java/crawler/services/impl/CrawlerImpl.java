package crawler.services.impl;

import crawler.filters.DomainFilter;
import crawler.logging.TxtReporter;
import crawler.services.Crawler;
import crawler.utils.HttpUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;


public class CrawlerImpl implements Crawler {

    private static final String DEFAULT_REPORTER_FILENAME = "report.txt";

    private final Queue<String> queue = new LinkedList<>();
    private final Set<String> visited = new HashSet<>();
    private final Set<String> seen = new HashSet<>();
    private final Map<String, String> urlToParentUrlMap = new HashMap<>();

    private final DomainFilter domainFilter;

    private final String reporterFilename;

    public CrawlerImpl(String url) {
        this(url, DEFAULT_REPORTER_FILENAME);
    }

    public CrawlerImpl(String url, String reportFileName) {
        this.domainFilter = new DomainFilter(url);
        this.queue.add(url);
        this.urlToParentUrlMap.put(url, null);
        this.reporterFilename = reportFileName;
    }

    public int crawl() {
        try (TxtReporter reporter = new TxtReporter(reporterFilename)) {
            while (!queue.isEmpty()) {
                String currentUrl = queue.poll();

                if (!visited.add(currentUrl)) continue;
                seen.add(currentUrl);

                crawler.models.PageResult page = HttpUtils.getPageResultBy(currentUrl);
                String parentUrl = urlToParentUrlMap.get(currentUrl);
                reporter.report(page, parentUrl);

                if (page.hasError()) continue;

                HttpUtils.extractUrlLinksBy(page.getDocument())
                        .stream()
                        .filter(domainFilter::accepts)
                        .filter(Predicate.not(seen::contains))
                        .forEach(link -> {
                            queue.add(link);
                            urlToParentUrlMap.put(link, currentUrl);
                        });
            }
        }
        return visited.size();
    }
}
