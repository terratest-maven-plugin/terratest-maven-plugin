package com.github.terratest.go;

import com.github.terratest.go.runtime.CommandWithPriority;

public interface GoRuntime {

    CommandWithPriority runtime(String goVersion);

    CommandWithPriority test(String goVersion);

    CommandWithPriority verbose(String goVersion);

    CommandWithPriority json(String goVersion);

    CommandWithPriority disableTestCaching(String goVersion);

    CommandWithPriority compile(String goVersion);

    CommandWithPriority version(String goVersion);
}
