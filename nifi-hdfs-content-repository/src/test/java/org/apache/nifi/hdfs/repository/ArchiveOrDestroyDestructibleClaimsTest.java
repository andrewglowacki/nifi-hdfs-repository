package org.apache.nifi.hdfs.repository;

import static org.apache.nifi.hdfs.repository.BinDestructibleClaimsTest.makeClaim;
import static org.apache.nifi.hdfs.repository.HdfsContentRepository.CORE_SITE_DEFAULT_PROPERTY;
import static org.apache.nifi.hdfs.repository.PropertiesBuilder.config;
import static org.apache.nifi.hdfs.repository.PropertiesBuilder.prop;
import static org.apache.nifi.hdfs.repository.PropertiesBuilder.props;
import static org.apache.nifi.util.NiFiProperties.REPOSITORY_CONTENT_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.apache.nifi.controller.repository.claim.ResourceClaim;
import org.apache.nifi.util.NiFiProperties;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class ArchiveOrDestroyDestructibleClaimsTest {

    //
    // These tests are basically the same.
    // One is verifying the archive method is called, the
    // other is verifying the remove method is called
    //

    @BeforeClass
    public static void setUpSuite() {
        Assume.assumeTrue("Test only runs on *nix", !SystemUtils.IS_OS_WINDOWS);
    }

    @Test
    public void archiveTest() throws Exception {
        executeTest(true);
    }

    @Test
    public void deleteTest() throws Exception {
        executeTest(false);
    }

    private void executeTest(boolean archiveEnabled) throws Exception {
        HdfsContentRepository repo = mock(HdfsContentRepository.class);
        when(repo.isArchiveEnabled()).thenReturn(archiveEnabled);
        when(repo.archiveClaim(any())).thenReturn(true);
        when(repo.remove(any(ResourceClaim.class))).thenReturn(true);

        NiFiProperties props = props(
            prop(REPOSITORY_CONTENT_PREFIX + "disk1", "target/test-repo1"),
            prop(REPOSITORY_CONTENT_PREFIX + "disk2", "target/test-repo2"),
            prop(CORE_SITE_DEFAULT_PROPERTY, "src/test/resources/empty-core-site.xml")
        );

        ContainerGroup group = new ContainerGroup(props, config(props), null, null);

        List<ResourceClaim> allClaims = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Container container = group.atModIndex(i);
            ResourceClaim claim = makeClaim(container.getName(), i);
            allClaims.add(claim);
            container.addReclaimableFile(claim);
        }
        assertEquals(100, allClaims.size());

        ArchiveOrDestroyDestructibleClaims claimHandler = new ArchiveOrDestroyDestructibleClaims(repo, group.getAll().values());

        claimHandler.run();

        if (archiveEnabled) {
            verify(repo, times(allClaims.size())).archiveClaim(any());
            verify(repo, times(0)).remove(any(ResourceClaim.class));
            for (ResourceClaim claim : allClaims) {
                verify(repo, times(1)).archiveClaim(eq(claim));
            }
        } else {
            verify(repo, times(0)).archiveClaim(any());
            verify(repo, times(allClaims.size())).remove(any(ResourceClaim.class));
            for (ResourceClaim claim : allClaims) {
                verify(repo, times(1)).remove(eq(claim));
            }
        }
    }
}
