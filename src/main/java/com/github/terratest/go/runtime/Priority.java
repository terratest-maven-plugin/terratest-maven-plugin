package com.github.terratest.go.runtime;

public enum Priority {

    P0((byte)0),
    P1((byte)1),
    P2((byte)2),
    P3((byte)3),
    P4((byte)4),
    P5((byte)5);

    private final byte priority;

    Priority(byte priority) {
        this.priority = priority;
    }

    public byte getPriority() {
        return priority;
    }
}
