package com.github.terratest.go;

import com.github.terratest.maven.plugin.generators.HtmlReportGenerator;
import com.github.terratest.maven.plugin.utils.AbstractTerraTestMojo;
import com.github.terratest.process.CommandResponse;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.terratest.utils.FileUtils.writeToFile;

/**
 * Abstraction over the Go client.
 */
public class GoRunner {

    private static final String TERRATEST_OUTPUT_LOG = "terratest-output.log";
    private static final String TERRATEST_ERROR_OUTPUT_LOG = "terratest-error-output.log";
    private final String terraTestsPath;
    private final Log logger;
    private final boolean useJsonOutput;
    private final boolean generateHtmlReport;
    private final boolean disableTestCaching;
    private final boolean createLogFile;
    private final List<String> arguments;

    private GoRunner(String terraTestsPath,
                     Log logger,
                     boolean useJsonOutput,
                     boolean generateHtmlReport,
                     boolean disableTestCaching,
                     boolean createLogFile,
                     List<String> arguments) {
        this.terraTestsPath = terraTestsPath;
        this.logger = logger;
        this.useJsonOutput = useJsonOutput;
        this.generateHtmlReport = generateHtmlReport;
        this.disableTestCaching = disableTestCaching;
        this.createLogFile = createLogFile;
        this.arguments = arguments;
    }

    /**
     * Checks whether the go runtime is present on the machine.
     * @throws MojoExecutionException If go runtime is not found.
     */
    public void checkGoPresence() throws MojoExecutionException {
        Optional<CommandResponse> maybeCommandResponse = GoClient
                .GoClientBuilder
                .newBuilder()
                .version()
                .build()
                .run();

        if (maybeCommandResponse.isPresent()) {
            CommandResponse commandResponse = maybeCommandResponse.get();
            if (commandResponse.isSuccess() && commandResponse.hasStdOut()) {
                logger.info("Go runtime is present");
            }
        } else {
            throw new MojoExecutionException("Can't find go runtime");
        }
    }

    /**
     * Runs the go test command with the params got from {@link GoRunnerBuilder}
     * @throws MojoFailureException If test running has failed
     * @throws IOException If log files or the HTML report can't be generated.
     */
    public void runGoTest() throws MojoFailureException, IOException {
        GoClient.GoClientBuilder goClientBuilder = GoClient
                .GoClientBuilder
                .newBuilder(terraTestsPath)
                .test()
                .verbose();

        if (useJsonOutput || generateHtmlReport) {
            logger.info("Using json output format");
            goClientBuilder.json();
        }
        if (disableTestCaching) {
            logger.info("Disable test caching");
            goClientBuilder.disableTestCaching();
        }

        Optional<CommandResponse> maybeCommandResponse = goClientBuilder
                .build()
                .runWithExtraArgs(arguments);

        boolean runSuccessful = false;
        String errorMessages = null;
        if (maybeCommandResponse.isPresent()) {
            CommandResponse commandResponse = maybeCommandResponse.get();
            if (!commandResponse.isSuccess()) {
                errorMessages = "There are failing terratests";
            } else {
                runSuccessful = true;
                logger.info("Go Test: OK");
            }
            generateLogfiles(commandResponse);
            generateHtmlReport(commandResponse);
        } else {
            errorMessages = "Can't run go test";
        }

        if (!runSuccessful) {
            throw new MojoFailureException(errorMessages);
        }
    }


    /**
     * Compiles the go tests in the {@link AbstractTerraTestMojo#getTerraTestPath()} directory
     * @throws MojoExecutionException If could not get the {@link CommandResponse}
     * @throws MojoFailureException If the compilation of tests resulted in error
     */
    public void compileGoTests() throws MojoExecutionException, MojoFailureException {
        Optional<CommandResponse> maybeCommandResponse = GoClient
                .GoClientBuilder
                .newBuilder(terraTestsPath)
                .test()
                .compile()
                .build()
                .runWithExtraArgs(arguments);

        if (maybeCommandResponse.isEmpty()) {
            throw new MojoExecutionException("Couldn't compile go test(s)");
        } else {
            final CommandResponse commandResponse = maybeCommandResponse.get();
            if (!commandResponse.isSuccess()) {
                throw new MojoFailureException("Failed to compile go test(s)");
            } else {
                logger.info("Go tests compiled OK");
            }
        }
    }

    /**
     * Builder class for the GoRunner.
     */
    public static class GoRunnerBuilder {

        private String terraTestsPath;
        private Log logger;
        private boolean useJsonOutput;
        private boolean generateHtmlReport;
        private boolean disableTestCaching;
        private boolean createLogFile;
        private List<String> arguments;

        private GoRunnerBuilder() {}

        /**
         * Returns an empty builder
         * @return {@link GoRunnerBuilder}
         */
        public static GoRunnerBuilder newBuilder() {
            return new GoRunnerBuilder();
        }

        /**
         * Sets the {@link AbstractTerraTestMojo#getTerraTestPath()} path where the go tests reside.
         * This is a mandatory field.
         * @param terraTestsPath The absolute path where the go tests are.
         * @return {@link GoRunnerBuilder}
         */
        public GoRunnerBuilder withTerraTestPath(final String terraTestsPath) {
            if(!Paths.get(terraTestsPath).isAbsolute()) {
                throw new IllegalArgumentException("terraTestPath must be absolute");
            }
            this.terraTestsPath = terraTestsPath;
            return this;
        }

        /**
         * Sets the logger for the GoRunner. This is a mandatory field.
         * @param logger The logger to use. Type must be {@link Log}
         * @return {@link GoRunnerBuilder} An intermediate GoRunnerBuilder
         */
        public GoRunnerBuilder withLogger(final Log logger) {
            if(logger == null) {
                throw new IllegalArgumentException("Logger can't be null");
            }
            this.logger = logger;
            return this;
        }

        /**
         * Whether or not use the -jsonOutput argument of go test
         * @see <a href="https://golang.org/pkg/cmd/go/internal/test/">Go Test reference</a>
         * @param useJsonOutput Boolean for setting the <code>-jsonOutput</code> parameter for go test
         * @return {@link GoRunnerBuilder} An intermediate GoRunnerBuilder
         */
        public GoRunnerBuilder useJsonOutput(final boolean useJsonOutput) {
           this.useJsonOutput = useJsonOutput;
            return this;
        }

        /**
         * Whether or not generate an HTML report of the test results in the
         * {@link AbstractTerraTestMojo#getTerraTestPath()} directory.
         * @param generateHtmlReport Boolean for generating HTML report from the go test results. If this is true, <code>useJsonOutput</code> will be set to true automatically.
         * @return {@link GoRunnerBuilder} An intermediate GoRunnerBuilder
         */
        public GoRunnerBuilder generateHtmlReport(final boolean generateHtmlReport) {
            this.generateHtmlReport = generateHtmlReport;
            if(generateHtmlReport) {
                useJsonOutput(true);
            }
            return this;
        }

        /**
         * Whether or not disable go test's caching mechanism
         * @see <a href="https://golang.org/pkg/cmd/go/internal/test/">Go Test reference</a>
         * @param disableTestCaching Boolean for disabling go test's caching mechanism
         * @return {@link GoRunnerBuilder} An intermediate GoRunnerBuilder
         */
        public GoRunnerBuilder disableCaching(final boolean disableTestCaching) {
            this.disableTestCaching = disableTestCaching;
            return this;
        }

        /**
         * Whether or not generate log files inside {@link AbstractTerraTestMojo#getTerraTestPath()} directory
         * containing the stdOut and stdErr of go test
         * @param createLogFile Boolean for setting whether generate log files or not
         * @return {@link GoRunnerBuilder} An intermediate GoRunnerBuilder
         */
        public GoRunnerBuilder createLogFile(final boolean createLogFile) {
            this.createLogFile = createLogFile;
            return this;
        }

        /**
         * List of any additional arguments wished to be passed to the go runtime.
         * For more Go arguments, @see <a href="https://golang.org/pkg/cmd/go/internal/test/">Go Test reference</a>
         * @param arguments Additional arguments for go test to be passed.
         * @return {@link GoRunnerBuilder} An intermediate GoRunnerBuilder
         */
        public GoRunnerBuilder withArguments(final List<String> arguments) {
            this.arguments = new ArrayList<>();
            if(arguments != null) {
                this.arguments.addAll(arguments);
            }
            return this;
        }

        /**
         * Check if <code>generateHtmlReport</code> is true and if so, it sets <code>useJsonOutput</code> to true also.
         * Checks if either <code>logger</code> or <code>terraTestsPath</code> is null, if so the builder throws an {@link IllegalStateException}
         * @return Fully built {@link GoRunner} instance.
         */
        public GoRunner build() {
            if(logger == null || terraTestsPath == null) {
                throw new IllegalStateException("Neither logger nor terraTestPath can be null!");
            }
            if(generateHtmlReport) {
                logger.info("generateHtmlReport is true, so setting useJsonOutput to true");
                useJsonOutput(true);
            }

            return new GoRunner(terraTestsPath,
                    logger,
                    useJsonOutput,
                    generateHtmlReport,
                    disableTestCaching,
                    createLogFile,
                    arguments);
        }
    }

    private void generateHtmlReport(CommandResponse commandResponse) throws IOException {
        if (generateHtmlReport) {
            logger.info("Generating HTML report to: " + terraTestsPath);
            HtmlReportGenerator.generateReport(commandResponse.getFullStdOut(), terraTestsPath);
        }
    }

    private void generateLogfiles(CommandResponse commandResponse) {
        if (createLogFile) {
            logger.info("Generating log files to: " + terraTestsPath);
            writeToFile(commandResponse.getStdOut(),
                    terraTestsPath + File.separator + TERRATEST_OUTPUT_LOG);
            writeToFile(commandResponse.getStdErr(),
                    terraTestsPath + File.separator + TERRATEST_ERROR_OUTPUT_LOG);
        }
    }

}
