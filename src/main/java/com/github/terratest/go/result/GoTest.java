package com.github.terratest.go.result;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

public class GoTest {

    private final String name;

    private final List<GoTestLine> goTestLines;

    private boolean isSuccess;

    private Double elapsed;

    public GoTest(String name, List<GoTestLine> goTestLines) {
        this.name = name;
        this.goTestLines = goTestLines;
        this.isSuccess = isGoTestASuccess(goTestLines);
        this.elapsed = gatherElapsed(goTestLines);
    }

    private Double gatherElapsed(List<GoTestLine> goTestLines) {
        for (GoTestLine goTestLine : goTestLines) {
            if (goTestLine.getElapsed() != null) {
                return goTestLine.getElapsed();
            }
        }
        return Double.NaN;
    }

    private boolean isGoTestASuccess(List<GoTestLine> goTestLines) {
        List<GoTestLine> reverseGoTestLines = Lists.reverse(goTestLines);
        for (GoTestLine goTestLine : reverseGoTestLines) {
            if (goTestLine.isPass()) {
                return true;
            }
        }

        return false;
    }

    public String getName() {
        return name;
    }

    public List<GoTestLine> getGoTestLines() {
        return goTestLines;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public Double getElapsed() {
        return elapsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoTest goTest = (GoTest) o;
        return isSuccess() == goTest.isSuccess() && Objects.equals(getName(), goTest.getName()) && Objects.equals(getGoTestLines(), goTest.getGoTestLines()) && Objects.equals(getElapsed(), goTest.getElapsed());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getGoTestLines(), isSuccess(), getElapsed());
    }

    @Override
    public String toString() {
        return "GoTest{" +
                "name='" + name + '\'' +
                ", goTestLines=" + goTestLines +
                ", isSuccess=" + isSuccess +
                ", elapsed=" + elapsed +
                '}';
    }
}
