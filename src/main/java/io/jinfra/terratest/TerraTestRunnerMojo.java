package io.jinfra.terratest;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Mojo(name = "run-tests", defaultPhase = LifecyclePhase.TEST)
public class TerraTestRunnerMojo extends AbstractTerraTestMojo{

    /**
     * Enables json output for go test
     * If ${@link TerraTestRunnerMojo#generateHtmlReport} is set to true this will be used too.
     * @see <a href="https://golang.org/pkg/cmd/go/internal/test/">Go Test reference</a>
     */
    @Parameter(defaultValue = "false", property = "terratest.useJsonOutput")
    private boolean useJsonOutput;

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
     * If this field is set to true {@link TerraTestRunnerMojo#useJsonOutput}
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
        GoClient goClient = GoClient.GoClientBuilder.newBuilder()
                .useJsonOutput(useJsonOutput)
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
            goClient.checkGoPresence();
            if (!Files.isDirectory(Paths.get(terraTestsPath))) {
                throw new MojoExecutionException("Can't find terratest basedir.");
            }
            goClient.runGoTest();
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }
}
