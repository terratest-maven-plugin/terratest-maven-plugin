package io.jinfra.terratest.tests;

import io.jinfra.terratest.TerraTestMojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

import java.io.File;

public class TerraTestMojoTest extends AbstractMojoTestCase {

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
        File file = new File("src/test/resources/test-pom.xml");
        TerraTestMojo terraTestMojo
                = (TerraTestMojo) lookupMojo("check",file);
        assertNotNull(terraTestMojo);
    }
}
