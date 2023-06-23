package org.apache.nifi.hdfs.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.nifi.controller.repository.claim.ResourceClaim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveOrDestroyDestructibleClaims implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ArchiveOrDestroyDestructibleClaims.class);
    private final Collection<Container> containers;
    private final AchievableRepository repository;

    public ArchiveOrDestroyDestructibleClaims(AchievableRepository repository, Collection<Container> containers) {
        this.containers = containers;
        this.repository = repository;
    }

    @Override
    public void run() {
        try {
            // while there are claims waiting to be destroyed...
            while (true) {
                // look through each of the binned queues of Content Claims
                int successCount = 0;
                final List<ResourceClaim> toRemove = new ArrayList<>();
                for (Container container : containers) {
                    // drain the queue of all ContentClaims that can be destroyed for the given container.

                    toRemove.clear();
                    container.drainReclaimable(toRemove);
                    if (toRemove.isEmpty()) {
                        continue;
                    }

                    // destroy each claim for this container
                    long start = System.nanoTime();
                    for (ResourceClaim claim : toRemove) {
                        try {
                            if (repository.isArchiveEnabled()) {
                                if (repository.archiveClaim(claim)) {
                                    successCount++;
                                }
                            } else if (repository.remove(claim)) {
                                successCount++;
                            }
                        } catch (Exception ex) {
                            LOG.warn("Failed to archive {} due to {}", claim, ex.toString());
                            if (LOG.isDebugEnabled()) {
                                LOG.warn("", ex);
                            }
                        }
                    }

                    final long nanos = System.nanoTime() - start;
                    final long millis = TimeUnit.NANOSECONDS.toMillis(nanos);

                    if (successCount == 0) {
                        LOG.debug("No ContentClaims archived/removed for Container {}", container);
                    } else {
                        LOG.info("Successfully {} {} Resource Claims for Container {} in {} millis", repository.isArchiveEnabled() ? "archived" : "destroyed", successCount, container, millis);
                    }
                }

                // if we didn't destroy anything, we're done.
                if (successCount == 0) {
                    return;
                }
            }
        } catch (final Throwable t) {
            LOG.error("Failed to handle destructible claims due to {}", t.toString());
            if (LOG.isDebugEnabled()) {
                LOG.error("", t);
            }
        }
    }
}
