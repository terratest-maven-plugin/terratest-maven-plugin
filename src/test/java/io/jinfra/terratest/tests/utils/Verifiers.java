package io.jinfra.terratest.tests.utils;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;

import java.util.function.Consumer;

import static org.junit.Assert.fail;

public class Verifiers {

    public static final Consumer<Verifier> NO_ERROR_VERIFIER
            = wrapper(Verifier::verifyErrorFreeLog);

    public static final Consumer<Verifier> LOG_FILES_SAVED
            = wrapper((Verifier verifier) -> {
                verifier.verifyErrorFreeLog();
                verifier.assertFilePresent("target/");
    });

    public static final Consumer<Verifier> FAILED_TERRATEST
            = wrapper((Verifier verifier) -> verifier.verifyTextInLog("exit status 1"));

    public static final Consumer<Verifier> NO_GO_FILE_PRESENT
            = wrapper((Verifier verifier) -> verifier.verifyTextInLog("no Go files"));

    public static final Consumer<Verifier> NO_DOCKER_FILE
            = wrapper((Verifier verifier) -> verifier.verifyTextInLog("no such file or directory"));


    static Consumer<Verifier> wrapper(VerifierExecutor verifierToExecute) {
        return (Verifier verifier) -> {
            try {
                verifierToExecute.execute(verifier);
            } catch (VerificationException e) {
                fail(e.getMessage());
            }
        };
    }

    @FunctionalInterface
    interface VerifierExecutor {
        void execute(Verifier verifier) throws VerificationException;
    }
}
