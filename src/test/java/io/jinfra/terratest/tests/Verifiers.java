package io.jinfra.terratest.tests;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static org.junit.Assert.fail;

public class Verifiers {

    public static final Consumer<Verifier> NO_ERROR_VERIFIER
            = wrapper(Verifier::verifyErrorFreeLog);

    public static final Consumer<Verifier> FAILED_TERRATEST
            = wrapper((Verifier verifier) -> verifier.verifyTextInLog("exit status 1"));

    public static final Consumer<Verifier> NO_GO_FILE_PRESENT
            = wrapper((Verifier verifier) -> verifier.verifyTextInLog("no Go files"));

    public static final Consumer<Verifier> NO_DOCKER_FILE
            = wrapper((Verifier verifier) -> verifier.verifyTextInLog("no such file or directory"));


    private static Consumer<Verifier> wrapper(VerifierExecutor verifierToExecute) {
        return (Verifier verifier) -> {
            try {
                verifierToExecute.execute(verifier);
            } catch (VerificationException e) {
                fail(e.getMessage());
            }
        };
    }

    @FunctionalInterface
    private interface VerifierExecutor {
        void execute(Verifier verifier) throws VerificationException;
    }
}
