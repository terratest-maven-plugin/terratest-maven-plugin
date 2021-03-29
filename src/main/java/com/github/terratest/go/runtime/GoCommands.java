package com.github.terratest.go.runtime;

public interface GoCommands {

    default CommandWithPriority goRuntime() {

        return new CommandWithPriority("go",Priority.P0);
    }

    default CommandWithPriority goTest() {

        return new CommandWithPriority("test", Priority.P1);
    }

    default CommandWithPriority version() {
        return new CommandWithPriority("version", Priority.P1);
    }

    default CommandWithPriority verbose() {

        return new CommandWithPriority("-v",Priority.P2);
    }

    default CommandWithPriority compile() {

        return new CommandWithPriority("-c", Priority.P3);
    }

    default CommandWithPriority json() {

        return new CommandWithPriority("-json", Priority.P4);
    }

    default CommandWithPriority disableTestCaching() {
        return new CommandWithPriority("-count=1",Priority.P5);
    }
}
