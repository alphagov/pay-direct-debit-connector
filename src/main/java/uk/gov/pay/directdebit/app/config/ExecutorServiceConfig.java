package uk.gov.pay.directdebit.app.config;

import io.dropwizard.Configuration;

public class ExecutorServiceConfig extends Configuration {

    private int threadsPerCpu;
    private int timeoutInSeconds;

    public int getThreadsPerCpu() {
        return threadsPerCpu;
    }

    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }
}
