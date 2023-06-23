package org.apache.nifi.hdfs.repository;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger index = new AtomicInteger();
    private final String name;

    public NamedThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(name + " #" + index.incrementAndGet());
        thread.setDaemon(false);
        return thread;
    }

}
