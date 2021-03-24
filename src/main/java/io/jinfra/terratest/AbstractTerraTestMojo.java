package io.jinfra.terratest;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * Containing all the common parameters used by all mojos:
 * {@link TerraTestCompileMojo}
 * {@link TerraTestRunnerMojo}
 */
public abstract class AbstractTerraTestMojo extends AbstractMojo {

    /**
     * The absolute path to the directory that contains the terratests to run.
     * Example:
     * <pre>
     * /-- example-maven-project
     *         -- terratests/
     *               ...
     *         -- src/
     *               ...
     *         -- pom.xml
     *</pre>
     * In this example the value of ${@link AbstractTerraTestMojo#terraTestPath} should be <i>${project.basedir}/terratests</i>
     */
    @Parameter(defaultValue = "terratest",
            property = "terratest.terraTestPath",
            required = true)
    private String terraTestPath;

    /**
     * Logs go test running output in files
     * The logs will be saved to {@link AbstractTerraTestMojo#terraTestPath}
     * Log files will be named:
     * <ul>
     *     <li>terratest-output.log: The StdOut of go test</li>
     *     <li>terratest-error-output.log: The StdErr of go test</li>
     * </ul>
     */
    @Parameter(defaultValue = "false", property = "terratest.createLogFile")
    private boolean createLogFile;

    /**
     * Extra arguments to pass to go test.
     * @see <a href="https://golang.org/pkg/cmd/go/internal/test/">Go Test reference</a>
     */
    @Parameter(property = "terratest.arguments")
    private List<String> arguments;

    /**
     * Enables json output for go test
     * @see <a href="https://golang.org/pkg/cmd/go/internal/test/">Go Test reference</a>
     */
    @Parameter(defaultValue = "false", property = "terratest.useJsonOutput")
    private boolean useJsonOutput;

    public String getTerraTestPath() {
        return terraTestPath;
    }

    public boolean isCreateLogFile() {
        return createLogFile;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public boolean isUseJsonOutput() {
        return useJsonOutput;
    }
}
