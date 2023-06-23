package org.apache.nifi.hdfs.repository;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.nifi.controller.repository.claim.StandardContentClaim;
import org.apache.nifi.stream.io.ByteCountingOutputStream;

public class ClaimOutputStream extends OutputStream {
    protected final StandardContentClaim claim;
    protected final ByteCountingOutputStream outStream;
    protected final ClaimClosedHandler handler;
    protected long bytesWritten = 0;
    protected boolean recycle = true;
    protected boolean closed = false;

    public ClaimOutputStream(ClaimClosedHandler handler, StandardContentClaim claim, ByteCountingOutputStream outStream) {
        this.handler = handler;
        this.claim = claim;
        this.outStream = outStream;
    }

    @Override
    public String toString() {
        return "ContentRepository Stream [" + claim + "]";
    }

    public boolean canRecycle() {
        return recycle;
    }
    public StandardContentClaim getClaim() {
        return claim;
    }
    public ByteCountingOutputStream getOutStream() {
        return outStream;
    }

    @Override
    public synchronized void write(final int b) throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }

        try {
            outStream.write(b);
        } catch (final IOException ioe) {
            recycle = false;
            throw new IOException("Failed to write to " + this, ioe);
        }

        bytesWritten++;
        claim.setLength(bytesWritten);
    }

    @Override
    public synchronized void write(final byte[] b) throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }

        try {
            outStream.write(b);
        } catch (final IOException ioe) {
            recycle = false;
            throw new IOException("Failed to write to " + this, ioe);
        }

        bytesWritten += b.length;
        claim.setLength(bytesWritten);
    }

    @Override
    public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }

        try {
            outStream.write(b, off, len);
        } catch (final IOException ioe) {
            recycle = false;
            throw new IOException("Failed to write to " + this, ioe);
        }

        bytesWritten += len;

        claim.setLength(bytesWritten);
    }

    @Override
    public synchronized void flush() throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }

        outStream.flush();
    }

    @Override
    public synchronized void close() throws IOException {
        closed = true;
        handler.claimClosed(this);
    }
}
