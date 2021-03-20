package io.jinfra.terratest;

import io.jinfra.testing.CommandResponse;
import io.jinfra.testing.ProcessRunner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Mojo(name = "check", defaultPhase = LifecyclePhase.TEST)
public class TerraTestMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * The relative path to the directory that contains the terratests to run.
     */
    @Parameter(defaultValue = "terratest",
            property = "terratest.relativePathToTests",
            required = true)
    private String relativePathToTerraTest;

    @Parameter(defaultValue = "false",
            property = "terratest.createLogFile")
    private boolean createLogFile;

    @Parameter(defaultValue = "false",
            property = "terratest.useJsonOutput")
    private boolean useJsonOutput;

    @Parameter(property = "terratest.arguments")
    private List<String> arguments;

    @Parameter(defaultValue = "false",
            property = "terratest.disableTestCaching")
    private boolean disableTestCaching;

    public TerraTestMojo() {
    }

    public void execute() throws MojoFailureException, MojoExecutionException {
        try {
            final String terraTestsPath = project.getBasedir().getCanonicalPath()
                    + File.separator + relativePathToTerraTest;
            getLog().info("TerraTest path: " + terraTestsPath);
            checkGoPresence();
            if (!Files.isDirectory(Paths.get(terraTestsPath))) {
                throw new MojoExecutionException("Can't find terratest basedir.");
            }
            runGoTest(terraTestsPath);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private void runGoTest(String terraTestsPath) throws MojoFailureException {
        final List<String> goTestCommand = new ArrayList<>();
        goTestCommand.add("go");
        goTestCommand.add("test");
        if (useJsonOutput) {
            getLog().info("Using json output format");
            goTestCommand.add("-json");
        }
        if(disableTestCaching) {
            goTestCommand.add("-count=1");
        }
        if(arguments != null && !arguments.isEmpty()) {
            for (String argument : arguments) {
                getLog().info("Adding argument to go test: " + argument);
                goTestCommand.add(argument);
            }
        }

        Optional<CommandResponse> maybeCommandResponse
                = ProcessRunner.runCommand(goTestCommand,
                new File(terraTestsPath),
                getLog()::info,
                getLog()::error);

        boolean runSuccessful = false;
        String errorMessages = null;
        if (maybeCommandResponse.isPresent()) {
            CommandResponse commandResponse = maybeCommandResponse.get();
            if (!Integer.valueOf(0).equals(commandResponse.getProcessResultCode())) {
                errorMessages = "There are failing terratests";
            } else {
                runSuccessful = true;
                getLog().info("Go Test: OK");
            }
        } else {
            errorMessages = "Can't run go test";
        }
        if (createLogFile && maybeCommandResponse.isPresent()) {
            final CommandResponse commandResponse = maybeCommandResponse.get();
            writeToFile(commandResponse.getStdOut(),
                    terraTestsPath + File.separator + "terratest-output.log");
            writeToFile(commandResponse.getStdErr(),
                    terraTestsPath + File.separator + "terratest-error-output.log");
        }
        if (!runSuccessful) {
            throw new MojoFailureException(errorMessages);
        }
    }

    private void checkGoPresence() throws MojoExecutionException {
        Optional<CommandResponse> maybeCommandResponse
                = ProcessRunner.runCommand(Arrays.asList("go", "version"));

        if (maybeCommandResponse.isPresent()) {
            CommandResponse commandResponse = maybeCommandResponse.get();
            if (Integer.valueOf(0).equals(commandResponse.getProcessResultCode())
                    && !commandResponse.getStdOut().isEmpty()) {
                getLog().info("Go version: " + commandResponse.getStdOut().get(0).trim());
            }
        } else {
            throw new MojoExecutionException("Can't find go runtime");
        }
    }

    private void writeToFile(List<String> content, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            for (String str : content) {
                writer.write(str + System.lineSeparator());
            }
        } catch (IOException e) {
            getLog().error("Can't save result to logfile: ", e);
        }
    }
}
