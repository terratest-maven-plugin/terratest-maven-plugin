package io.jinfra.terratest.tests;

import org.junit.Test;

import java.io.File;

import static io.jinfra.terratest.tests.utils.VerificationRunner.*;

public class MavenITmng1234TestTerraTestRun {

    private static final String TEST_FIXTURE_NAME = "mng-1234";


    @Test
    public void testSavingLogs() {
        final String terraTestDir = "docker-test";
        File testDir = getTestProject("saving-log", TEST_FIXTURE_NAME);
        assertTestLogsHaveBeenCreated(testDir,terraTestDir);
    }

    @Test
    public void testCachingDisables() {
        File testDir = getTestProject("disable-test-caching", TEST_FIXTURE_NAME);
        assertNoErrorHasBeenReported(testDir);
    }

    @Test
    public void testFailingTerraTest() {
        File testDir = getTestProject("failing-terratest", TEST_FIXTURE_NAME);
        assertTerraTestFailing(testDir);
    }

    @Test
    public void testValidDockerTerraTestRunSuccessfully() {
        File testDir = getTestProject("valid-docker-test", TEST_FIXTURE_NAME);
        assertNoErrorHasBeenReported(testDir);
    }

    @Test
    public void testDummyPomLoad() {
        File testDir = getTestProject("dummy", TEST_FIXTURE_NAME);
        assertNoGoFileToTestIsPresent(testDir);
    }

    @Test
    public void testDockerFileMissing() {
        File testDir = getTestProject("missing-dockerfile", TEST_FIXTURE_NAME);
        assertMissingDockerfile(testDir);
    }
}
