package com.github.terratest.process;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandResponse {

    private final List<String> stdOut;

    private final List<String> stdErr;

    private final Integer processResultCode;

    public CommandResponse(List<String> stdOut,
                           List<String> stdErr,
                           Integer processResultCode) {
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.processResultCode = processResultCode;
    }

    public List<String> getStdOut() {
        return stdOut;
    }

    public List<String> getStdErr() {
        return stdErr;
    }

    public Integer getProcessResultCode() {
        return processResultCode;
    }

    public boolean isSuccess() {
        return Integer.valueOf(0).equals(processResultCode);
    }

    public boolean hasStdOut() {
        return stdOut != null && !stdOut.isEmpty();
    }

    public String getFullStdOut() {
        return stdOut
                .stream()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        CommandResponse that = (CommandResponse) object;
        return Objects.equals(getStdOut(), that.getStdOut())
                && Objects.equals(getStdErr(), that.getStdErr())
                && Objects.equals(getProcessResultCode(), that.getProcessResultCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStdOut(),
                getStdErr(),
                getProcessResultCode());
    }

    @Override
    public String toString() {
        return "CommandResponse{" +
                "stdOut=" + stdOut +
                ", stdErr=" + stdErr +
                ", processResultCode=" + processResultCode +
                '}';
    }
}
