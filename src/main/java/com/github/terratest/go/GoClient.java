package com.github.terratest.go;

import com.github.terratest.go.runtime.CommandWithPriority;
import com.github.terratest.process.CommandResponse;
import com.github.terratest.process.ProcessRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GoClient {

    private final List<String> commands;

    private final String terraTestsPath;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoClient.class);

    public GoClient(List<String> commands, String terraTestsPath) {
        this.commands = commands;
        this.terraTestsPath = terraTestsPath;
    }

    public Optional<CommandResponse> run() {
            return ProcessRunner.runCommand(commands,
                    terraTestsPath == null ? null : new File(terraTestsPath),
                    LOGGER::info,
                    LOGGER::error);
    }

    public Optional<CommandResponse> runWithExtraArgs(List<String> extraArgs) {
        if (extraArgs != null && extraArgs.size() != 0) {
            commands.addAll(extraArgs);
        }
        return run();
    }

    public static class GoClientBuilder {

        private final List<CommandWithPriority> commands;

        private String goVersion;

        private final GoRuntime goRuntime = new DefaultGoRuntime();

        private String terraTestPath;

        public GoClientBuilder(String terraTestPath, String goVersion) {
            this(terraTestPath);
            this.goVersion = goVersion;
        }

        public GoClientBuilder(String terraTestPath) {
            this.goVersion = null;
            this.terraTestPath = terraTestPath;
            commands = new ArrayList<>();
            commands.add(goRuntime.runtime(goVersion));
            commands.add(goRuntime.test(goVersion));
        }

        public GoClientBuilder verbose() {
            commands.add(goRuntime.verbose(goVersion));
            return this;
        }

        public GoClientBuilder compile() {
            commands.add(goRuntime.compile(goVersion));
            return this;
        }

        public GoClientBuilder json() {
            commands.add(goRuntime.json(goVersion));
            return this;
        }

        public GoClientBuilder disableTestCaching() {
            commands.add(goRuntime.disableTestCaching(goVersion));
            return this;
        }

        public GoClientBuilder version() {
            commands.add(goRuntime.version(goVersion));
            return this;
        }

        public static GoClientBuilder newBuilder(String terraTestPath) {
            return new GoClientBuilder(terraTestPath);
        }

        public static GoClientBuilder newBuilder() {
            return new GoClientBuilder(null);
        }

        public static GoClientBuilder newBuilderWithVersion(String terraTestPath, String goVersion) {
            return new GoClientBuilder(terraTestPath,goVersion);
        }

        public GoClient build() {
            List<String> commandsToRun = commands
                    .stream()
                    .sorted()
                    .map(CommandWithPriority::getCommand)
                    .collect(Collectors.toList());
            return new GoClient(commandsToRun,terraTestPath);
        }
    }

}
