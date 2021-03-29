package com.github.terratest.process;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

//TODO: Create builder
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

    public CommandResponse(Integer processResultCode) {
        this.processResultCode = processResultCode;
        this.stdOut = Collections.emptyList();
        this.stdErr = Collections.emptyList();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandResponse that = (CommandResponse) o;
        return Objects.equals(getStdOut(), that.getStdOut())
                && Objects.equals(getStdErr(), that.getStdErr())
                && Objects.equals(getProcessResultCode(), that.getProcessResultCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStdOut(), getStdErr(), getProcessResultCode());
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
