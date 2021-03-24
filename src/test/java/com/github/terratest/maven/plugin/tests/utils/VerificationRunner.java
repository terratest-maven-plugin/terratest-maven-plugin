package com.github.terratest.maven.plugin.tests.utils;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class VerificationRunner {

    public static void runVerification(File testDir,
                                       Consumer<Verifier> verificationMethod) {
        runVerification(testDir, verificationMethod, Collections.emptyList());
    }

    public static void runVerification(File testDir,
                                       Consumer<Verifier> verificationMethod,
                                       List<String> extraArgs) {
        try {
            /*
             * We must first make sure that any artifact created
             * by this test has been removed from the local
             * repository. Failing to do this could cause
             * unstable test results. Fortunately, the verifier
             * makes it easy to do this.
             */
            Verifier verifier = new Verifier(testDir.getAbsolutePath());
            verifier.deleteDirectory("target");

            final String dirName = testDir.getName();

            final String fullName = ApplicationTestConstant.TERRATEST_MAVEN_PLUGIN_BASE_NAME + dirName;

            verifier.deleteArtifact(ApplicationTestConstant.TERRATEST_MAVEN_PLUGIN_BASE_PACKAGE + fullName, fullName + "-" + ApplicationTestConstant.PLUGIN_VERSION, ApplicationTestConstant.PLUGIN_VERSION, "jar");
            verifier.deleteArtifact(ApplicationTestConstant.TERRATEST_MAVEN_PLUGIN_BASE_PACKAGE + fullName, fullName + "-" + ApplicationTestConstant.PLUGIN_VERSION, ApplicationTestConstant.PLUGIN_VERSION, "pom");
            verifier.deleteArtifact(ApplicationTestConstant.TERRATEST_MAVEN_PLUGIN_BASE_PACKAGE + fullName, "maven-metadata-local", ApplicationTestConstant.PLUGIN_VERSION, "xml");
            verifier.deleteArtifact(ApplicationTestConstant.TERRATEST_MAVEN_PLUGIN_BASE_PACKAGE + fullName, "_remote", ApplicationTestConstant.PLUGIN_VERSION, ".repositories");

            /*
             * The Command Line Options (CLI) are passed to the
             * verifier as a list. This is handy for things like
             * redefining the local repository if needed. In
             * this case, we use the -N flag so that Maven won't
             * recurse. We are only installing the parent pom to
             * the local repo here.
             */
            List<String> cliOptions = new ArrayList<>();
            cliOptions.add("-N");
            verifier.setCliOptions(cliOptions);
            List<String> arguments = new ArrayList<>();
            arguments.add("clean");
            arguments.add("install");
            arguments.addAll(extraArgs);
            try {
                verifier.executeGoals(arguments);
            } catch (VerificationException e) {
                // Just because exit code is non-zero,
                // we still want to verify with verificationMethod
                if (!e.getMessage().contains("Exit code was non-zero")) {
                    System.out.println("Error: Exit code was non-zero:" + e);
                    throw e;
                }
            }

            /*
             * This is the simplest way to check a build
             * succeeded. It is also the simplest way to create
             * an IT test: make the build pass when the test
             * should pass, and make the build fail when the
             * test should fail. There are other methods
             * supported by the verifier. They can be seen here:
             * http://maven.apache.org/shared/maven-verifier/apidocs/index.html
             */
            verificationMethod.accept(verifier);

            verifier.resetStreams();

            /*
             * The verifier also supports beanshell scripts for
             * verification of more complex scenarios. There are
             * plenty of examples in the core-it tests here:
             * https://svn.apache.org/repos/asf/maven/core-integration-testing/trunk
             */
        } catch (IOException | VerificationException e) {
            fail(e.getMessage());
        }
    }

    public static File getTestProject(TestResource testResource) {
        try {
            return ResourceExtractor
                    .simpleExtractResources(VerificationRunner.class,
                            File.separator
                                    + testResource.getTestFixture()
                                    + File.separator
                                    + testResource.getMavenProject());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return null;
    }

    public static void assertNoErrorHasBeenReported(File testDir) {
        runVerification(testDir, Verifiers.NO_ERROR_VERIFIER);
    }

    public static void assertNoGoFileToTestIsPresent(File testDir) {
        runVerification(testDir, Verifiers.NO_GO_FILE_PRESENT);
    }

    public static void assertTerraTestFailing(File testDir) {
        runVerification(testDir, Verifiers.FAILED_TERRATEST);
    }

    public static void assertMissingDockerfile(File testDir) {
        runVerification(testDir, Verifiers.NO_DOCKER_FILE);
    }

    public static void assertTestLogsHaveBeenCreated(File testDir, String terraTestDir) {
        runVerification(testDir, Verifiers.wrapper((Verifier verifier) -> {
            final String testMavenProjectTerraTestDir = String.join(File.separator,
                    testDir.getParent(),
                    testDir.getName(),
                    terraTestDir);
            verifier.verifyErrorFreeLog();
            verifier.assertFilePresent(String.join(File.separator,
                    testMavenProjectTerraTestDir,
                    ApplicationTestConstant.OUTPUT_LOG));
            verifier.assertFilePresent(String.join(File.separator,
                    testMavenProjectTerraTestDir,
                    ApplicationTestConstant.ERROR_OUTPUT_LOG));
        }) );
    }

    public static void assertHtmlReportHasBeenCreatedSuccessfulTests(File testDir, String terraTestDir) {
        runVerification(testDir, Verifiers.wrapper((Verifier verifier) -> {
            final String testMavenProjectTerraTestDir = String.join(File.separator,
                    testDir.getParent(),
                    testDir.getName(),
                    terraTestDir);
            verifier.verifyErrorFreeLog();
            verifier.assertFilePresent(String.join(File.separator,
                    testMavenProjectTerraTestDir,
                    ApplicationTestConstant.GENERATED_HTML_REPORT));
        }) );
    }

    public static void assertHtmlReportHasBeenCreatedFailedTests(File testDir, String terraTestDir) {
        runVerification(testDir, Verifiers.wrapper((Verifier verifier) -> {
            final String testMavenProjectTerraTestDir = String.join(File.separator,
                    testDir.getParent(),
                    testDir.getName(),
                    terraTestDir);
            Verifiers.FAILED_TERRATEST.accept(verifier);
            verifier.assertFilePresent(String.join(File.separator,
                    testMavenProjectTerraTestDir,
                    ApplicationTestConstant.GENERATED_HTML_REPORT));
        }) );
    }
}
