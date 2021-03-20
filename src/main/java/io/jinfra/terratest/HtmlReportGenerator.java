package io.jinfra.terratest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.jinfra.terratest.go.test.result.GoTest;
import io.jinfra.terratest.go.test.result.GoTestLine;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class HtmlReportGenerator {

    private static final ObjectReader OBJECT_READER;

    private static final String HTML_TEMPLATE_PATH = "html-report-template";

    static {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(new JavaTimeModule());
        OBJECT_READER = objectMapper.reader();
    }

    public static void generateReport(List<String> goTestResults, final String path) throws IOException {
        Instant start = Instant.now();
        if (goTestResults == null
                || goTestResults.isEmpty()
                || path == null
                || path.isEmpty()) {
            throw new IllegalArgumentException();
        }

        String[] lines = StringUtils
                .split(goTestResults.get(0), System.lineSeparator());

        Map<String, List<GoTestLine>> rawGoTests = new HashMap<>();
        for (String goTestResult : lines) {
            GoTestLine goTestLine = OBJECT_READER.readValue(goTestResult, GoTestLine.class);

            //TODO: Try to rework this.
            final String testName = goTestLine.getTest();
            if (testName == null) {
                continue;
            }
            if (rawGoTests.containsKey(testName)) {
                List<GoTestLine> linesForTest = rawGoTests.get(testName);
                linesForTest.add(goTestLine);
            } else {
                List<GoTestLine> linesForTest = new ArrayList<>();
                linesForTest.add(goTestLine);
                rawGoTests.put(testName, linesForTest);
            }
        }

        List<GoTest> goTests = new ArrayList<>();
        for (Map.Entry<String, List<GoTestLine>> entry : rawGoTests.entrySet()) {
            final GoTest goTest = new GoTest(entry.getKey(), entry.getValue());
            goTests.add(goTest);
        }


        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(HTML_TEMPLATE_PATH + File.separator + "index.mustache");
        Instant end = Instant.now();


        final long runTime = Duration.between(start,end).toMillis();
        final PrintWriter printWriter = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(path + File.separator + "index.html"),
                        StandardCharsets.UTF_8.name())));
        mustache.execute(printWriter, new GoTestWrapper(goTests,runTime)).flush();
    }

    private static class GoTestWrapper {

        private final List<GoTest> goTests;

        private final long numberOfTests;

        private final long numberOfSuccessfulTests;

        private final long numberOfFailedTests;

        private final Double totalElapsed;

        private final long generatedIn;

        public GoTestWrapper(List<GoTest> goTests, long generatedIn) {
            this.goTests = goTests;
            this.numberOfTests = goTests.size();
            numberOfSuccessfulTests = goTests.stream().filter(GoTest::isSuccess).count();
            this.numberOfFailedTests = numberOfTests - numberOfSuccessfulTests;
            this.totalElapsed = goTests.stream().mapToDouble(f -> f.getElapsed()).sum();
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

        public Double getTotalElapsed() {
            return totalElapsed;
        }

        public long getGeneratedIn() {
            return generatedIn;
        }
    }
}
