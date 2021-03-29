package com.github.terratest.go.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.Objects;

public class GoTestLine {

    private final ZonedDateTime time;

    private final String action;

    private final String testPackage;

    private final String test;

    private final String output;

    private final Double elapsed;

    @JsonCreator
    public GoTestLine(@JsonProperty("Time") ZonedDateTime time,
                      @JsonProperty("Action") String action,
                      @JsonProperty("Package") String testPackage,
                      @JsonProperty("Test") String test,
                      @JsonProperty("Output") String output,
                      @JsonProperty("Elapsed") Double elapsed) {
        this.time = time;
        this.action = action;
        this.testPackage = testPackage;
        this.test = test;
        this.output = output;
        this.elapsed = elapsed;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public String getAction() {
        return action;
    }

    public String getTestPackage() {
        return testPackage;
    }

    public String getTest() {
        return test;
    }

    public String getOutput() {
        return output;
    }

    public boolean isPass() {
        return "pass".equals(action);
    }

    public Double getElapsed() {
        return elapsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoTestLine that = (GoTestLine) o;
        return Objects.equals(getTime(),
                that.getTime()) && Objects.equals(getAction(),
                that.getAction()) && Objects.equals(getTestPackage(),
                that.getTestPackage()) && Objects.equals(getTest(),
                that.getTest()) && Objects.equals(getOutput(),
                that.getOutput()) && Objects.equals(getElapsed(), that.getElapsed());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(),
                getAction(),
                getTestPackage(),
                getTest(),
                getOutput(),
                getElapsed());
    }

    @Override
    public String toString() {
        return "GoTestLine{" +
                "time=" + time +
                ", action='" + action + '\'' +
                ", testPackage='" + testPackage + '\'' +
                ", test='" + test + '\'' +
                ", output='" + output + '\'' +
                ", elapsed=" + elapsed +
                '}';
    }
}
