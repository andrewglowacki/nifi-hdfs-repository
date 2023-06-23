package org.apache.nifi.hdfs.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.nifi.controller.repository.claim.ResourceClaim;
import org.apache.nifi.controller.repository.claim.ResourceClaimManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapted from FileSystemRepository.
 *
 * Retrieves claims that are no longer being used the claim manager
 * and puts them into container based queues for destruction/archiving.
 *
 * Does this need to be a separate thread?
 */
public class BinDestructibleClaims implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BinDestructibleClaims.class);

    private final ResourceClaimManager claimManager;
    private final Map<String, Container> containers;

    public BinDestructibleClaims(ResourceClaimManager claimManager, Map<String, Container> containers) {
        this.claimManager = claimManager;
        this.containers = containers;
    }

    @Override
    public void run() {
        try {
            // Get all of the Destructible Claims and bin them based on their Container. We do this
            // because the Container generally maps to a physical partition on the disk, so we want a few
            // different threads hitting the different partitions but don't want multiple threads hitting
            // the same partition.
            List<ResourceClaim> toDestroy = new ArrayList<>();
            while (true) {
                toDestroy.clear();
                claimManager.drainDestructableClaims(toDestroy, 10000);
                if (toDestroy.isEmpty()) {
                    return;
                }

                for (ResourceClaim claim : toDestroy) {
                    String containerName = claim.getContainer();
                    Container container = containers.get(containerName);
                    if (container == null) {
                        LOG.warn("Failed to clean up {} due to unknown container: {}", claim, containerName);
                        continue;
                    }

                    try {
                        while (!container.addReclaimableFile(claim)) {
                            LOG.warn("Failed to clean up {} because old claims aren't being cleaned up fast enough. "
                                    + "This Content Claim will remain in the Content Repository until NiFi is restarted, at which point it will be cleaned up", claim);
                        }
                    } catch (InterruptedException ie) {
                        LOG.warn("Failed to clean up {} because thread was interrupted", claim);
                    }
                }
            }
        } catch (Throwable t) {
            LOG.error("Failed to cleanup content claims due to {}", t);
        }
    }

}
