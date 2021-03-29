package com.github.terratest.maven.plugin;

import com.github.terratest.go.GoRunner;
import com.github.terratest.maven.plugin.utils.AbstractTerraTestMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.time.Duration;
import java.time.Instant;

/**
 * Compiles the terratests in {@link AbstractTerraTestMojo#getTerraTestPath()}
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE)
public class TerraTestCompileMojo extends AbstractTerraTestMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        GoRunner goRunner = GoRunner.GoRunnerBuilder.newBuilder()
                .useJsonOutput(isUseJsonOutput())
                .createLogFile(isCreateLogFile())
                .withTerraTestPath(getTerraTestPath())
                .withArguments(getArguments())
                .withLogger(getLog())
                .build();
        Instant start = Instant.now();
        goRunner.checkGoPresence();
        goRunner.compileGoTests();
        Instant end = Instant.now();
        final long duration = Duration.between(start,end).toMillis();
        getLog().info("Go tests have been successfully compiled in " + duration + "ms.");
    }
}
