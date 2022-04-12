package com.github.terratest.maven.plugin.tests;

import com.github.terratest.maven.plugin.tests.utils.TestResource;
import org.junit.Test;

import java.io.File;

import static com.github.terratest.maven.plugin.tests.utils.VerificationRunner.*;

public class TerraTestRunnerMojoIT {

    @Test
    public void testHtmlReportWithOneFailingOneSuccessfulTerratest() {
        final TestResource testResource = TestResource.TWO_TESTS_ONE_FAILING_ONE_SUCCESSFUL;
        File testDir = getTestProject(testResource);
        assertHtmlReportHasBeenCreatedFailedTests(testDir,testResource.getTerraTestPath());
    }

    @Test
    public void testHtmlReportGeneration() {
        final TestResource testResource = TestResource.HTML_TEST_REPORT;
        File testDir = getTestProject(testResource);
        assertHtmlReportHasBeenCreatedSuccessfulTests(testDir,testResource.getTerraTestPath());
    }


    @Test
    public void testSavingLogs() {
        final TestResource testResource = TestResource.SAVING_LOG;
        File testDir = getTestProject(testResource);
        assertTestLogsHaveBeenCreated(testDir,testResource.getTerraTestPath());
    }

    @Test
    public void testCachingDisables() {
        File testDir = getTestProject(TestResource.DISABLE_TEST_CACHING);
        assertNoErrorHasBeenReported(testDir);
    }

    @Test
    public void testFailingTerraTestWithHtmlReport() {
        final TestResource testResource = TestResource.FAILING_TERRATEST_WITH_HTML_REPORT;
        File testDir = getTestProject(testResource);
        assertHtmlReportHasBeenCreatedFailedTests(testDir,testResource.getTerraTestPath());
    }

    @Test
    public void testFailingTerraTest() {
        File testDir = getTestProject(TestResource.FAILING_TERRATEST);
        assertTerraTestFailing(testDir);
    }

    @Test
    public void testValidDockerTerraTestRunSuccessfully() {
        File testDir = getTestProject(TestResource.VALID_DOCKER_TEST);
        assertNoErrorHasBeenReported(testDir);
    }

    @Test
    public void testDummyPomLoad() {
        File testDir = getTestProject(TestResource.PROJECT_WITHOUT_DOCKERFILE);
        assertNoGoFileToTestIsPresent(testDir);
    }

    @Test
    public void testDockerFileMissing() {
        File testDir = getTestProject(TestResource.MISSING_DOCKERFILE);
        assertMissingDockerfile(testDir);
    }
    
    @Test
    public void testTimeoutOverride() {
        File testDir = getTestProject(TestResource.TIMEOUT_OVERRIDE);
        assertNoErrorHasBeenReported(testDir);
    }
}
