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


    public static class GoClientBuilder {

        private String terraTestsPath;
        private Log logger;
        private boolean useJsonOutput;
        private boolean generateHtmlReport;
        private boolean disableTestCaching;
        private boolean createLogFile;
        private List<String> arguments;

        private GoClientBuilder() {}

        public static GoClientBuilder newBuilder() {
            return new GoClientBuilder();
        }

        public GoClientBuilder withTerraTestPath(final String terraTestsPath) {
            if(!Paths.get(terraTestsPath).isAbsolute()) {
                throw new IllegalArgumentException("terraTestPath mus be absolute");
            }
            this.terraTestsPath = terraTestsPath;
            return this;
        }

        public GoClientBuilder withLogger(final Log logger) {
            if(logger == null) {
                throw new IllegalArgumentException("Logger can't be null");
            }
            this.logger = logger;
            return this;
        }

        public GoClientBuilder useJsonOutput(final boolean useJsonOutput) {
           this.useJsonOutput = useJsonOutput;
            return this;
        }

        public GoClientBuilder generateHtmlReport(final boolean generateHtmlReport) {
            this.generateHtmlReport = generateHtmlReport;
            if(generateHtmlReport) {
                useJsonOutput(true);
            }
            return this;
        }

        public GoClientBuilder disableCaching(final boolean disableTestCaching) {
            this.disableTestCaching = disableTestCaching;
            return this;
        }

        public GoClientBuilder createLogFile(final boolean createLogFile) {
            this.createLogFile = createLogFile;
            return this;
        }

        public GoClientBuilder withArguments(final List<String> arguments) {
            this.arguments = new ArrayList<>();
            if(arguments != null) {
                this.arguments.addAll(arguments);
            }
            return this;
        }

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
