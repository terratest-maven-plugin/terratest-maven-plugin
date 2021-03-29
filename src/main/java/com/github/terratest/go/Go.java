package com.github.terratest.go;

import com.github.terratest.go.runtime.CommandWithPriority;
import com.github.terratest.go.runtime.GoCommands;
import com.github.terratest.go.runtime.Commands_v_116;

public enum Go {

    V1_16(new Commands_v_116());

    private GoCommands goCommands;

    Go(GoCommands goCommands) {
        this.goCommands = goCommands;
    }

     public CommandWithPriority goRuntime() {
        return goCommands.goRuntime();
    }

    public CommandWithPriority goTest() {
        return goCommands.goTest();
    }

    public CommandWithPriority verbose() {
        return goCommands.verbose();
    }

    public CommandWithPriority compile() {
        return goCommands.compile();
    }

    public CommandWithPriority json() {
        return goCommands.json();
    }

    public CommandWithPriority version() {
        return goCommands.version();
    }

    public CommandWithPriority disableTestCaching() {
        return goCommands.disableTestCaching();
    }


}
