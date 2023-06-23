package org.apache.nifi.hdfs.repository;

import java.io.IOException;

public interface ClaimClosedHandler {

    /**
     * Called to handle the behavior of what happens when a claim's output
     * stream is closed. Note: the backing output stream will not yet be closed when
     * this is called - the handler is responsible for closing the stream if applicable
     * with: outStream.getOutStream().close()
     */
    public void claimClosed(ClaimOutputStream claimStream) throws IOException;
}
