package com.github.terratest.maven.plugin;

import com.github.terratest.go.GoRunner;
import com.github.terratest.maven.plugin.utils.AbstractTerraTestMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Runs the terratests {@link AbstractTerraTestMojo#getTerraTestPath()}
 */
@Mojo(name = "run-tests", defaultPhase = LifecyclePhase.TEST)
public class TerraTestRunnerMojo extends AbstractTerraTestMojo{

    /**
     * Extra arguments to pass to go test.
     * @see <a href="https://golang.org/pkg/cmd/go/internal/test/">Go Test reference</a>
     */
    @Parameter(property = "terratest.arguments")
    private List<String> arguments;

    /**
     * Disables test caching.
     * @see <a href="https://golang.org/pkg/cmd/go/internal/test/">Go Test reference</a>
     */
    @Parameter(defaultValue = "false", property = "terratest.disableTestCaching")
    private boolean disableTestCaching;

    /**
     * Generated test report as HTML into {@link AbstractTerraTestMojo#getTerraTestPath()}
     * If this field is set to true {@link TerraTestRunnerMojo#isUseJsonOutput()}
     * will be set true automatically.
     */
    @Parameter(defaultValue = "false", property = "terratest.generateHtmlReport")
    private boolean generateHtmlReport;

    @Parameter(defaultValue = "false", property = "skipTests", readonly = true)
    private boolean skipTests;

    public void execute() throws MojoFailureException, MojoExecutionException {
        if (skipTests) {
            getLog().info("Skipping terratest run");
            return;
        }
        GoRunner goRunner = GoRunner.GoRunnerBuilder.newBuilder()
                .useJsonOutput(isUseJsonOutput())
                .createLogFile(isCreateLogFile())
                .disableCaching(disableTestCaching)
                .withTerraTestPath(getTerraTestPath())
                .generateHtmlReport(generateHtmlReport)
                .withArguments(arguments)
                .withLogger(getLog())
                .build();
        try {
            final String terraTestsPath = getTerraTestPath();
            getLog().info("TerraTest path: " + terraTestsPath);
            goRunner.checkGoPresence();
            if (!Files.isDirectory(Paths.get(terraTestsPath))) {
                throw new MojoExecutionException("Can't find terratest basedir.");
            }
            goRunner.runGoTest();
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }
}
