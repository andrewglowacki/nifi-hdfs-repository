package org.apache.nifi.hdfs.repository;

import java.io.IOException;

import org.apache.nifi.controller.repository.ContentRepository;
import org.apache.nifi.controller.repository.claim.ResourceClaim;

public interface AchievableRepository extends ContentRepository {

    public boolean isArchiveEnabled();

    public boolean archiveClaim(ResourceClaim claim) throws IOException;

    public boolean remove(ResourceClaim claim) throws IOException;
}
