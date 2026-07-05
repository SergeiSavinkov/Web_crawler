# Web Crawler

This repository contains three versions of the same crawler:

- `sequential` - single-threaded
- `multithreaded` - parallel with worker threads
- `distributed` - distributed with MPJ

## Requirements

- Java 25
- Maven 3.9+
- For `distributed`, MPJ Express is also required. `mpjrun.sh` is the external launcher that comes with MPJ
Express

All modules use the same default start URL: `https://famnit.upr.si`.

If you want to crawl a different site:

- in `sequential` and `multithreaded`, change the URL directly in `Main.java`
- in `distributed`, pass the URL as a command-line argument

## Sequential version

From the `sequential` directory:

```bash
mvn clean compile exec:java
```

The result and report are written to `report.txt` in the module directory.

## Multithreaded version

From the `multithreaded` directory:

```bash
mvn clean compile exec:java
```

The result and report are written to `report.txt` in the module directory.

## Distributed version

From the `distributed` directory, first build the project and copy dependencies:

```bash
mvn clean compile dependency:copy-dependencies
```

Then run it with the MPJ launcher from your MPJ Express installation:

```bash
mpjrun.sh -np 4 -cp "target/classes:target/dependency/*:lib/mpj.jar" crawler.Main https://famnit.upr.si
```

You can replace `4` with the desired number of processes. This version requires at least 2 processes.

The report is also written to `report.txt` in the module directory.

## Notes

- Run each command from the corresponding module directory.
- If you change the URL in code, rebuild the project before running it.
- `report.txt` is overwritten on each run.
