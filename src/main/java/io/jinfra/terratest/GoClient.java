package io.jinfra.terratest;

import io.jinfra.testing.CommandResponse;
import io.jinfra.testing.ProcessRunner;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Abstraction over the Go client.
 */
public class GoClient {

    private final String terraTestsPath;
    private final Log logger;
    private final boolean useJsonOutput;
    private final boolean generateHtmlReport;
    private final boolean disableTestCaching;
    private final boolean createLogFile;
    private final List<String> arguments;

    private GoClient(String terraTestsPath,
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
        Optional<CommandResponse> maybeCommandResponse
                = ProcessRunner.runCommand(createCommandList("go", "version"));

        if (maybeCommandResponse.isPresent()) {
            CommandResponse commandResponse = maybeCommandResponse.get();
            if (Integer.valueOf(0).equals(commandResponse.getProcessResultCode())
                    && !commandResponse.getStdOut().isEmpty()) {
                logger.info("Go version: " + commandResponse.getStdOut().get(0).trim());
            }
        } else {
            throw new MojoExecutionException("Can't find go runtime");
        }
    }

    /**
     * Runs the go test command with the params got from {@link GoClientBuilder}
     * @throws MojoFailureException If test running has failed
     * @throws IOException If log files or the HTML report can't be generated.
     */
    public void runGoTest() throws MojoFailureException, IOException {
        final List<String> goTestCommand = createCommandList("go","test","-v");

        if (useJsonOutput || generateHtmlReport) {
            logger.info("Using json output format");
            goTestCommand.add("-json");
        }
        if (disableTestCaching) {
            goTestCommand.add("-count=1");
        }
        addExtraArguments(goTestCommand);

        Optional<CommandResponse> maybeCommandResponse = runCommand(goTestCommand);

        boolean runSuccessful = false;
        String errorMessages = null;
        if (maybeCommandResponse.isPresent()) {
            CommandResponse commandResponse = maybeCommandResponse.get();
            if (!Integer.valueOf(0).equals(commandResponse.getProcessResultCode())) {
                errorMessages = "There are failing terratests";
            } else {
                runSuccessful = true;
                logger.info("Go Test: OK");
            }
        } else {
            errorMessages = "Can't run go test";
        }
        if (createLogFile && maybeCommandResponse.isPresent()) {
            logger.info("Generating log files to: " + terraTestsPath);
            final CommandResponse commandResponse = maybeCommandResponse.get();
            writeToFile(commandResponse.getStdOut(),
                    terraTestsPath + File.separator + "terratest-output.log");
            writeToFile(commandResponse.getStdErr(),
                    terraTestsPath + File.separator + "terratest-error-output.log");
        }

        if (generateHtmlReport && maybeCommandResponse.isPresent()) {
            logger.info("Generating HTML repo to: " + terraTestsPath);
            final CommandResponse commandResponse = maybeCommandResponse.get();
            HtmlReportGenerator.generateReport(commandResponse.getStdOut(), terraTestsPath);
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
        final List<String> goTestCommand = createCommandList("go","test","-c");
        addExtraArguments(goTestCommand);

        Optional<CommandResponse> maybeCommandResponse = runCommand(goTestCommand);

        if(maybeCommandResponse.isEmpty()) {
            throw new MojoExecutionException("Couldn't compile go test(s)");
        } else {
            final CommandResponse commandResponse = maybeCommandResponse.get();
            if (!Integer.valueOf(0).equals(commandResponse.getProcessResultCode())) {
                throw new MojoFailureException("Failed to compile go test(s)");
            } else {
                logger.info("Go tests successfully compiled");
            }
        }
    }

    private Optional<CommandResponse> runCommand(List<String> goTestCommand) {
        return ProcessRunner.runCommand(goTestCommand,
                new File(terraTestsPath),
                logger::info,
                logger::error);
    }

    private void addExtraArguments(List<String> goTestCommand) {
        if (arguments != null && !arguments.isEmpty()) {
            for (String argument : arguments) {
                logger.info("Adding argument to go test: " + argument);
                goTestCommand.add(argument);
            }
        }
    }

    private void writeToFile(List<String> content, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {
            for (String str : content) {
                writer.write(str + System.lineSeparator());
            }
        } catch (IOException e) {
            logger.error("Can't save result to logfile: ", e);
        }
    }

    private List<String> createCommandList(String... commands) {
        return new ArrayList<>(Arrays.asList(commands));
    }


    /**
     * Builder class for the GoClient.
     */
    public static class GoClientBuilder {

        private String terraTestsPath;
        private Log logger;
        private boolean useJsonOutput;
        private boolean generateHtmlReport;
        private boolean disableTestCaching;
        private boolean createLogFile;
        private List<String> arguments;

        private GoClientBuilder() {}

        /**
         * Returns an empty builder
         * @return {@link GoClientBuilder}
         */
        public static GoClientBuilder newBuilder() {
            return new GoClientBuilder();
        }

        /**
         * Sets the {@link AbstractTerraTestMojo#getTerraTestPath()} path where the go tests reside.
         * This is a mandatory field.
         * @param terraTestsPath The absolute path where the go tests are.
         * @return {@link GoClientBuilder}
         */
        public GoClientBuilder withTerraTestPath(final String terraTestsPath) {
            if(!Paths.get(terraTestsPath).isAbsolute()) {
                throw new IllegalArgumentException("terraTestPath mus be absolute");
            }
            this.terraTestsPath = terraTestsPath;
            return this;
        }

        /**
         * Sets the logger for the GoClient. This is a mandatory field.
         * @param logger The logger to use. Type must be {@link Log}
         * @return {@link GoClientBuilder} An intermediate GoClientBuilder
         */
        public GoClientBuilder withLogger(final Log logger) {
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
         * @return {@link GoClientBuilder} An intermediate GoClientBuilder
         */
        public GoClientBuilder useJsonOutput(final boolean useJsonOutput) {
           this.useJsonOutput = useJsonOutput;
            return this;
        }

        /**
         * Whether or not generate an HTML report of the test results in the
         * {@link AbstractTerraTestMojo#getTerraTestPath()} directory.
         * @param generateHtmlReport Boolean for generating HTML report from the go test results. If this is true, <code>useJsonOutput</code> will be set to true automatically.
         * @return {@link GoClientBuilder} An intermediate GoClientBuilder
         */
        public GoClientBuilder generateHtmlReport(final boolean generateHtmlReport) {
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
         * @return {@link GoClientBuilder} An intermediate GoClientBuilder
         */
        public GoClientBuilder disableCaching(final boolean disableTestCaching) {
            this.disableTestCaching = disableTestCaching;
            return this;
        }

        /**
         * Whether or not generate log files inside {@link AbstractTerraTestMojo#getTerraTestPath()} directory
         * containing the stdOut and stdErr of go test
         * @param createLogFile Boolean for setting whether generate log files or not
         * @return {@link GoClientBuilder} An intermediate GoClientBuilder
         */
        public GoClientBuilder createLogFile(final boolean createLogFile) {
            this.createLogFile = createLogFile;
            return this;
        }

        /**
         * List of any additional arguments wished to be passed to the go runtime.
         * For more Go arguments, @see <a href="https://golang.org/pkg/cmd/go/internal/test/">Go Test reference</a>
         * @param arguments Additional arguments for go test to be passed.
         * @return {@link GoClientBuilder} An intermediate GoClientBuilder
         */
        public GoClientBuilder withArguments(final List<String> arguments) {
            this.arguments = new ArrayList<>();
            if(arguments != null) {
                this.arguments.addAll(arguments);
            }
            return this;
        }

        /**
         * Check if <code>generateHtmlReport</code> is true and if so, it sets <code>useJsonOutput</code> to true also.
         * Checks if either <code>logger</code> or <code>terraTestsPath</code> is null, if so the builder throws an {@link IllegalStateException}
         * @return Fully built {@link GoClient} instance.
         */
        public GoClient build() {
            if(logger == null || terraTestsPath == null) {
                throw new IllegalStateException("Neither logger nor terraTestPath can be null!");
            }
            if(generateHtmlReport) {
                logger.info("generateHtmlReport is true, so setting useJsonOutput to true");
                useJsonOutput(true);
            }

            return new GoClient(terraTestsPath,
                    logger,
                    useJsonOutput,
                    generateHtmlReport,
                    disableTestCaching,
                    createLogFile,
                    arguments);
        }
    }
}
