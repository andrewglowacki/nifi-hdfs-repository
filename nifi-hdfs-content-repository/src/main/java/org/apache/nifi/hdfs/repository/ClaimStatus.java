package org.apache.nifi.hdfs.repository;

import org.apache.hadoop.fs.Path;

public class ClaimStatus {
    private final long size;
    private final Path path;
    private final Container container;

    public ClaimStatus(long size, Path path, Container container) {
        this.size = size;
        this.path = path;
        this.container = container;
    }

    public long getSize() {
        return size;
    }
    public Path getPath() {
        return path;
    }
    public Container getContainer() {
        return container;
    }

}
