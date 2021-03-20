package io.jinfra.terratest.tests.utils;

import java.io.File;

public class ApplicationTestConstant {

    public static final String OUTPUT_LOG = "terratest-output.log";
    public static final String ERROR_OUTPUT_LOG = "terratest-error-output.log";
    public static final String GENERATED_HTML_REPORT = "index.html";
    public static final String TERRATEST_MAVEN_PLUGIN_BASE_NAME = "terratest-maven-plugin-";
    public static final String TERRATEST_MAVEN_PLUGIN_BASE_PACKAGE = "org.apache.maven.io.jinfra.";
    public static final String PLUGIN_VERSION = "0.0.1-SNAPSHOT";
    public static final String TEST_RESOURCES_BASE_DIR = String.join(File.separator,"src","test","resources");
}
