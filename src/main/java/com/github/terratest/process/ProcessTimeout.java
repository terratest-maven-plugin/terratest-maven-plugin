package com.github.terratest.process;

import java.util.concurrent.TimeUnit;

public class ProcessTimeout {
	private final long timeoutValue;
	private final TimeUnit timeUnit;
	
	public ProcessTimeout(long timeoutValue, TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
		this.timeoutValue = timeoutValue;
	}
	
	public long getTimeoutValue() {
		return timeoutValue;
	}
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}
}
