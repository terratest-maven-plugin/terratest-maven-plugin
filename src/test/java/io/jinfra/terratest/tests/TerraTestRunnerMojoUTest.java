package io.jinfra.terratest.tests;

import io.jinfra.terratest.TerraTestRunnerMojo;
import io.jinfra.terratest.tests.utils.TestResource;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

import java.io.File;

import static io.jinfra.terratest.tests.utils.ApplicationTestConstant.TEST_RESOURCES_BASE_DIR;

public class TerraTestRunnerMojoUTest extends AbstractMojoTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testNoTerraTest() throws Exception {
        final TestResource testResource = TestResource.BASIC_TEST_UNIT;
        File file = new File(String.join(File.separator,
                TEST_RESOURCES_BASE_DIR,
                testResource.getTestFixture(),
                testResource.getMavenProject(),
                "pom.xml"));
        TerraTestRunnerMojo terraTestRunnerMojo
                = (TerraTestRunnerMojo) lookupMojo("run-tests",file);
        assertNotNull(terraTestRunnerMojo);
    }
}
