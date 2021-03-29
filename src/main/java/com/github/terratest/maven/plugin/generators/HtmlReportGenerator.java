package com.github.terratest.maven.plugin.generators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.terratest.go.result.GoTest;
import com.github.terratest.go.result.GoTestLine;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class HtmlReportGenerator {

    private static final ObjectReader OBJECT_READER;
    private static final String HTML_TEMPLATE_PATH = "html-report-template";
    private static final MustacheFactory MUSTACHE_FACTORY = new DefaultMustacheFactory();
    private static final String TEMPLATE_FILE = "index.mustache";
    private static final String HTML_REPORT = "index.html";

    private HtmlReportGenerator() {}

    static {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(new JavaTimeModule());
        OBJECT_READER = objectMapper.reader();
    }

    public static void generateReport(String goTestResults, final String path) throws IOException {
        Instant start = Instant.now();
        if (goTestResults == null
                || goTestResults.isEmpty()
                || path == null
                || path.isEmpty()) {
            throw new IllegalArgumentException();
        }

        //Needs to be got as a String and then
        // separate again as some process send it as multiple lines some
        // as one big chunk of string
        String[] lines = StringUtils
                .split(goTestResults, System.lineSeparator());

        Map<String, List<GoTestLine>> rawGoTests = new HashMap<>();
        for (String goTestResult : lines) {
            GoTestLine goTestLine = OBJECT_READER.readValue(goTestResult, GoTestLine.class);

            final String testName = goTestLine.getTest();
            if (testName == null) {
                continue;
            }
            if (!rawGoTests.containsKey(testName)) {
                rawGoTests.put(testName, new ArrayList<>());

            }
            rawGoTests.get(testName).add(goTestLine);
        }

        List<GoTest> goTests = new ArrayList<>();
        for (Map.Entry<String, List<GoTestLine>> entry : rawGoTests.entrySet()) {
            final GoTest goTest = new GoTest(entry.getKey(), entry.getValue());
            goTests.add(goTest);
        }

        Mustache mustache = MUSTACHE_FACTORY
                .compile(HTML_TEMPLATE_PATH + File.separator + TEMPLATE_FILE);
        Instant end = Instant.now();


        final long runTime = Duration.between(start,end).toMillis();
        final PrintWriter printWriter = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(path + File.separator + HTML_REPORT),
                        StandardCharsets.UTF_8.name())));
        mustache.execute(printWriter, new GoTestWrapper(goTests,runTime)).flush();
    }

    private static class GoTestWrapper {

        private final List<GoTest> goTests;

        private final List<GoTest> successfulTests;

        private final List<GoTest> failedTests;

        private final long numberOfTests;

        private final long numberOfSuccessfulTests;

        private final long numberOfFailedTests;

        private final String totalElapsed;

        private final long generatedIn;

        public GoTestWrapper(List<GoTest> goTests, long generatedIn) {
            this.goTests = goTests;
            this.successfulTests = goTests
                    .stream()
                    .filter(GoTest::isSuccess)
                    .collect(Collectors.toList());
            this.failedTests = new ArrayList<>(CollectionUtils.subtract(goTests,successfulTests));
            this.numberOfTests = goTests.size();
            this.numberOfSuccessfulTests = successfulTests.size();
            this.numberOfFailedTests = numberOfTests - numberOfSuccessfulTests;
            this.totalElapsed = String.format("%.2f", goTests.stream()
                    .mapToDouble(GoTest::getElapsed)
                    .sum());
            this.generatedIn = generatedIn;
        }

        List<GoTest> goTests() {
            return goTests;
        }

        public List<GoTest> getGoTests() {
            return goTests;
        }

        public long getNumberOfTests() {
            return numberOfTests;
        }

        public long getNumberOfSuccessfulTests() {
            return numberOfSuccessfulTests;
        }

        public long getNumberOfFailedTests() {
            return numberOfFailedTests;
        }

        public String getTotalElapsed() {
            return totalElapsed;
        }

        public long getGeneratedIn() {
            return generatedIn;
        }

        public List<GoTest> getSuccessfulTests() {
            return successfulTests;
        }

        public List<GoTest> getFailedTests() {
            return failedTests;
        }
    }
}
