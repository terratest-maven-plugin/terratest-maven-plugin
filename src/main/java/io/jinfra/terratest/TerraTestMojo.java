package io.jinfra.terratest;

import io.jinfra.testing.CommandResponse;
import io.jinfra.testing.ProcessRunner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

@Mojo(name = "check")
public class TerraTestMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * The relative path to the directory that contains the terratests to run.
     */
    @Parameter(defaultValue = "terratest", property = "terratest.relativePathToTests",  required = true)
    private String relativePathToTerraTest;

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

    private void runGoTest(String terraTestsPath) throws MojoFailureException, MojoExecutionException {
        Optional<CommandResponse> maybeCommandResponse
                = ProcessRunner.runCommand(Arrays.asList("go", "test"),
                new File(terraTestsPath),
                getLog()::info,
                getLog()::error);

        if (maybeCommandResponse.isPresent()) {
            CommandResponse commandResponse = maybeCommandResponse.get();
            if (!Integer.valueOf(0).equals(commandResponse.getProcessResultCode())) {
                throw new MojoFailureException("There are failing terratests");
            } else {
                getLog().info("Go Test: OK");
            }
        } else {
            throw new MojoExecutionException("Can't run go test");
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
}
