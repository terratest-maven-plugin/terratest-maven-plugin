package io.jinfra.terratest.tests;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static io.jinfra.terratest.tests.Verifiers.*;

public class MavenITmng1234TestTerraTestRun {

        private static final String TEST_FIXTURE_NAME = "mng-1234";

        @Test
        public void testFailingTerraTest() {
            File testDir = getTestProject("failing-terratest");
            assertTerraTestFailing(testDir);
        }

        @Test
        public void testValidDockerTerraTestRunSuccessfully() {
            File testDir = getTestProject("valid-docker-test");
            assertNoErrorHasBeenReported(testDir);
        }

        @Test
        public void testDummyPomLoad() {
            File testDir = getTestProject("dummy");
            assertNoGoFileToTestIsPresent(testDir);
        }

        @Test
        public void testDockerFileMissing() {
            File testDir = getTestProject("missing-dockerfile");
            assertMissingDockerfile(testDir);
        }

        private void assertNoErrorHasBeenReported(File testDir) {
            runVerification(testDir, NO_ERROR_VERIFIER);
        }

        private void assertNoGoFileToTestIsPresent(File testDir) {
            runVerification(testDir, NO_GO_FILE_PRESENT);
        }

        private void assertTerraTestFailing(File testDir) {
            runVerification(testDir,FAILED_TERRATEST);
        }

        private void assertMissingDockerfile(File testDir) {
            runVerification(testDir,NO_DOCKER_FILE);
        }

        private void runVerification(File testDir, Consumer<Verifier> verificationMethod) {
            try {
                /*
                 * We must first make sure that any artifact created
                 * by this test has been removed from the local
                 * repository. Failing to do this could cause
                 * unstable test results. Fortunately, the verifier
                 * makes it easy to do this.
                 */
                Verifier verifier = new Verifier(testDir.getAbsolutePath());

                final String dirName = testDir.getName();

                verifier.deleteArtifact("org.apache.maven.io.jinfra." + dirName, "maven-metadata-local", "0.0.1-SNAPSHOT", "xml");

                verifier.deleteArtifact("org.apache.maven.io.jinfra." + dirName, "parent", "0.0.1-SNAPSHOT", "pom");
                verifier.deleteArtifact("org.apache.maven.io.jinfra." + dirName, "checkstyle-test", "0.0.1-SNAPSHOT", "jar");
                verifier.deleteArtifact("org.apache.maven.io.jinfra." + dirName, "checkstyle-assembly", "0.0.1-SNAPSHOT", "jar");
                verifier.deleteArtifact("org.apache.maven.io.jinfra." + dirName, dirName + "-0.0.1-SNAPSHOT", "0.0.1-SNAPSHOT", "jar");
                verifier.deleteArtifact("org.apache.maven.io.jinfra." + dirName, dirName + "-0.0.1-SNAPSHOT", "0.0.1-SNAPSHOT", "pom");


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
                try {
                    verifier.executeGoals(Arrays.asList("clean", "install"));
                } catch (VerificationException e) {
                    // Just because exit code is non-zero,
                    // we still want to verify with verificationMethod
                    if(!e.getMessage().contains("Exit code was non-zero")) {
                        //LOGGER.error("Error: Exit code was non-zero:", e);
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

                /*
                 * The verifier also supports beanshell scripts for
                 * verification of more complex scenarios. There are
                 * plenty of examples in the core-it tests here:
                 * https://svn.apache.org/repos/asf/maven/core-integration-testing/trunk
                 */
            } catch (IOException | VerificationException e) {
                //LOGGER.error("Error happened while running verification on {}: ", testDir, e);
            }
        }

        private File getTestProject(String projectName) {
            // Check in your dummy Maven project in /src/test/resources/...
            // The testdir is computed from the location of this
            // file.
            try {
                return ResourceExtractor
                        .simpleExtractResources(getClass(),
                                File.separator + TEST_FIXTURE_NAME + File.separator + projectName + "-maven-project");
            } catch (IOException e) {
                //LOGGER.error("Error loading test maven project {}: ", projectName, e);
            }
            return null;
        }
}
