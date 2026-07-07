package crawler;

import crawler.services.Crawler;
import crawler.services.impl.MpjDistributedCrawlerImpl;
import mpi.MPI;

// cd distributed
// Assembly: mvn clean compile
// Execution:
// mpjrun.bat -np 4 -cp "target/classes;target/dependency/jsoup-1.18.2.jar;lib/mpj.jar" crawler.Main https://famnit.upr.si

public class Main {

    private static final String DEFAULT_URL = "https://famnit.upr.si";
    private static final int TEST_PAGE_LIMIT = 500;

    public static void main(String[] args) {
        args = MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int processCount = MPI.COMM_WORLD.Size();

        if (rank == 0) {
            String url = args.length > 0 ? args[0] : DEFAULT_URL;

            long start = System.currentTimeMillis();
            Crawler crawler = new MpjDistributedCrawlerImpl(url, "report.txt", processCount /* TEST_PAGE_LIMIT */);
            int pages = crawler.crawl();
            long end = System.currentTimeMillis();

            /*
            double seconds = (end - start) / 1000.0;
            double pagesPerSecond = pages / seconds;
            System.out.println("Visited pages: " + pages);
            System.out.printf("Time: %.2f s%n", seconds);
            System.out.printf("Pages per second: %.2f%n", pagesPerSecond);
            */

            System.out.println("Visited pages: " + pages);
            System.out.println("Time: " + (end - start) + " ms");
        } else {
            MpjDistributedCrawlerImpl.runWorker();
        }

        MPI.Finalize();
    }
}
