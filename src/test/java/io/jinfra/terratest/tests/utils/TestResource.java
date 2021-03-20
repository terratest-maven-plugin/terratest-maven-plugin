package io.jinfra.terratest.tests.utils;

import static io.jinfra.terratest.tests.utils.TestFixture.TERRA_TEST_RUNNER_MOJO_TEST_FIXTURE;

public enum TestResource {

    DISABLE_TEST_CACHING(TERRA_TEST_RUNNER_MOJO_TEST_FIXTURE),
    FAILING_TERRATEST(TERRA_TEST_RUNNER_MOJO_TEST_FIXTURE),
    FAILING_TERRATEST_WITH_HTML_REPORT("docker-test", TERRA_TEST_RUNNER_MOJO_TEST_FIXTURE),
    HTML_TEST_REPORT("docker-test", TERRA_TEST_RUNNER_MOJO_TEST_FIXTURE),
    MISSING_DOCKERFILE(TERRA_TEST_RUNNER_MOJO_TEST_FIXTURE),
    PROJECT_WITHOUT_DOCKERFILE(TERRA_TEST_RUNNER_MOJO_TEST_FIXTURE),
    SAVING_LOG("docker-test", TERRA_TEST_RUNNER_MOJO_TEST_FIXTURE),
    TWO_TESTS_ONE_FAILING_ONE_SUCCESSFUL("docker-test", TERRA_TEST_RUNNER_MOJO_TEST_FIXTURE),
    VALID_DOCKER_TEST(TERRA_TEST_RUNNER_MOJO_TEST_FIXTURE),
    BASIC_TEST_UNIT(TERRA_TEST_RUNNER_MOJO_TEST_FIXTURE);


    private static final String MAVEN_PROJECT_POSTFIX = "-maven-project";

    private final String terraTestPath;
    private final TestFixture testFixture;

    TestResource(String terraTestPath, TestFixture testFixture) {
        this.terraTestPath = terraTestPath;
        this.testFixture = testFixture;
    }

    TestResource(TestFixture testFixture) {
        this.terraTestPath = null;
        this.testFixture = testFixture;
    }

    public String getMavenProject() {
        final String enumName = this
                .name()
                .toLowerCase()
                .replace("_", "-");
        return enumName + MAVEN_PROJECT_POSTFIX;
    }

    public String getTerraTestPath() {
        return terraTestPath;
    }

    public String getTestFixture() {
        return testFixture.getFixtureDirectory();
    }
}
