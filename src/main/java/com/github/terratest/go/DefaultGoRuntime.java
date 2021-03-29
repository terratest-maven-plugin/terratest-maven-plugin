package com.github.terratest.go;

import com.github.terratest.go.runtime.CommandWithPriority;

import java.util.HashMap;
import java.util.Map;

public class DefaultGoRuntime implements GoRuntime {

    public static final Go DEFAULT_VERSION = Go.V1_16;

    private final Map<String, Go> supportedRunTimeVersions;

    public DefaultGoRuntime() {
        supportedRunTimeVersions = new HashMap<>();
        supportedRunTimeVersions.put(Go.V1_16.name(), DEFAULT_VERSION);
    }

    @Override
    public CommandWithPriority runtime(String goVersion) {
        return getGo(goVersion).goRuntime();
    }

    @Override
    public CommandWithPriority test(String goVersion) {
        return getGo(goVersion).goTest();
    }

    @Override
    public CommandWithPriority verbose(String goVersion) {
        return getGo(goVersion).verbose();
    }

    @Override
    public CommandWithPriority json(String goVersion) {
        return getGo(goVersion).json();
    }

    @Override
    public CommandWithPriority disableTestCaching(String goVersion) {
        return getGo(goVersion).disableTestCaching();
    }

    @Override
    public CommandWithPriority compile(String goVersion) {
        return getGo(goVersion).compile();
    }

    @Override
    public CommandWithPriority version(String goVersion) {
        return getGo(goVersion).version();
    }

    private Go getGo(String goVersion) {
        return supportedRunTimeVersions
                .computeIfAbsent(goVersion, (goVersion1) -> DEFAULT_VERSION);
    }
}
