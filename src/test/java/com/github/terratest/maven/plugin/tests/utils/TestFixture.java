package com.github.terratest.maven.plugin.tests.utils;

public enum TestFixture {
    TERRA_TEST_RUNNER_MOJO_TEST_FIXTURE("terra-test-runner-mojo");

    private final String fixtureDirectory;

    TestFixture(String fixtureDirectory) {
        this.fixtureDirectory = fixtureDirectory;
    }

    public String getFixtureDirectory() {
        return fixtureDirectory;
    }
}
