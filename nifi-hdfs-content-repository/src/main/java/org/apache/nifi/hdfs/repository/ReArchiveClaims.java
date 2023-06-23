package org.apache.nifi.hdfs.repository;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReArchiveClaims implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ReArchiveClaims.class);

    private final Container archiveFrom;
    private final ContainerGroup archiveTo;
    private final int sectionsPerContainer;

    public ReArchiveClaims(Container from, ContainerGroup archive, int sectionsPerContainer) {
        this.archiveTo = archive;
        this.archiveFrom = from;
        this.sectionsPerContainer = sectionsPerContainer;
    }

    @Override
    public void run() {
        try {
            long duration = System.currentTimeMillis();

            int count = 0;

            FileSystem fromFs = archiveFrom.getFileSystem();
            Path path = archiveFrom.getPath();

            for (int i = 0; i < sectionsPerContainer; i++) {
                Path copyFrom = new Path(new Path(path, "" + i), HdfsContentRepository.ARCHIVE_DIR_NAME);
                try {
                    RemoteIterator<FileStatus> files = fromFs.listStatusIterator(copyFrom);
                    while (files.hasNext()) {
                        Path copy = files.next().getPath();
                        String claimId = copy.getName();
                        if (claimId.startsWith(".")) {
                            continue;
                        }

                        Path to = null;
                        try {
                            Container toContainer = archiveTo.atModIndex(claimId.hashCode());
                            FileSystem toFs = toContainer.getFileSystem();

                            String toSubPath = "" + i + "/" + HdfsContentRepository.ARCHIVE_DIR_NAME + "/" + claimId;
                            to = new Path(toContainer.getPath(), toSubPath);

                            try (FSDataInputStream inStream = fromFs.open(copy)) {
                                try (FSDataOutputStream outStream = toFs.create(to)) {
                                    StreamUtils.copy(inStream, outStream);
                                }
                            }

                            if (!fromFs.delete(copy, false)) {
                                LOG.warn("Failed to delete successfully re-archived file: {}", copy);
                            }
                            count++;
                        } catch (IOException ex) {
                            // we should set a failure condition here, but it's ambiguous which container is at fault
                            LOG.warn("Failed to rearchive from " + copy + " to " + to, ex);
                        }
                    }
                } catch (FileNotFoundException ex) { }
            }

            duration = System.currentTimeMillis() - duration;

            if (count == 0) {
                LOG.debug("No ContentClaims rearchived for {}", archiveFrom);
            } else {
                LOG.info("Successfully rearchived {} Resource Claims in {} ms for {}", count, duration, archiveFrom);
            }

        } catch (Throwable ex) {
            LOG.error("Failed to rearchive claims", ex);
        }
    }


}
