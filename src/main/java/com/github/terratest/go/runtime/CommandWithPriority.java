package com.github.terratest.go.runtime;

import java.util.Objects;

public class CommandWithPriority implements Comparable<CommandWithPriority> {

    private final String command;

    private final Priority priority;

    public CommandWithPriority(String command, Priority priority) {
        this.command = command;
        this.priority = priority;
    }

    public String getCommand() {
        return command;
    }

    public Priority getPriority() {
        return priority;
    }

    @Override
    public int compareTo(CommandWithPriority o) {
        return Byte.compare(getPriority().getPriority(),
                o.getPriority().getPriority());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandWithPriority that = (CommandWithPriority) o;
        return Objects.equals(getCommand(), that.getCommand()) && getPriority() == that.getPriority();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommand(), getPriority());
    }

    @Override
    public String toString() {
        return "CommandWithPriority{" +
                "command='" + command + '\'' +
                ", priority=" + priority +
                '}';
    }
}
