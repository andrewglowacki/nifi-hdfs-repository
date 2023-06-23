package org.apache.nifi.hdfs.repository;

/**
 * Thrown when all the containers in a group are inactive due to their disk's
 * being full or failures
 */
public class NoActiveContainersException extends InterruptedException {

    private static final long serialVersionUID = 1L;

}
