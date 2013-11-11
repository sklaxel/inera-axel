/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.broker.messagestore.internal;

import com.mongodb.gridfs.GridFSDBFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.exception.OtherErrorException;

import javax.mail.internet.SharedInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A stream that buffers data retrieved from an underlying file stored in GridFS.
 * This stream supports mark, reset and it is also possible to retrieve a sub stream
 * via the newStream method.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
class GridFsSharedInputStream extends InputStream implements SharedInputStream {
    private static final Logger LOG = LoggerFactory.getLogger(GridFsSharedInputStream.class);
    private final InputStream inputStream;

    private GridFSDBFile gridFSDBFile;
    private long offset;
    private long endPosition;
    private long position;
    private long mark = -1;

    public GridFsSharedInputStream(GridFSDBFile gridFSDBFile) {
        this(gridFSDBFile, 0, gridFSDBFile.getLength());
        LOG.debug("GridFsSharedInputStream(GridFSDBFile)");
    }

    public GridFsSharedInputStream(GridFSDBFile gridFSDBFile, long offset, long length) {
        LOG.debug("GridFsSharedInputStream(GridFSDBFile, {}, {})", offset, length);
        this.inputStream = new BufferedInputStream(gridFSDBFile.getInputStream());
        try {
            this.inputStream.skip(offset);
        } catch (IOException e) {
            // TODO which exception should we throw?
            throw new OtherErrorException("Failed to create stream", e);
        }
        this.gridFSDBFile = gridFSDBFile;
        this.endPosition = Math.min(offset + length, gridFSDBFile.getLength());
        this.offset = offset;
        this.position = offset;
    }

    @Override
    public boolean markSupported() {
        LOG.debug("markSupported");
        return true;
    }

    @Override
    public synchronized void mark(int readlimit) {
        LOG.debug("mark");
        this.mark = this.position;
        this.inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        LOG.debug("reset");
        if (this.mark < 0)
            throw new IOException("reset() called with invalid mark position");

        this.inputStream.reset();

        this.position = this.mark;
    }

    @Override
    public long getPosition() {
        long currentPosition = position - offset;
        LOG.debug("getPosition return {}", currentPosition);
        return currentPosition;
    }

    @Override
    public long skip(long n) throws IOException {
        long skip;
        if (n > (this.endPosition - this.position)) {
            skip = this.endPosition - this.position;
        } else {
            skip = n;
        }

        long skipped = this.inputStream.skip(skip);
        this.position += skipped;

        return skipped;
    }

    @Override
    public InputStream newStream(long start, long end) {
        LOG.debug("newStream({}, {})", start, end);
        if (start < 0) {
            throw new IllegalArgumentException("Start position of stream must be non negative");
        }

        long newStreamStart = this.offset + start;
        long newStreamLength;

        if (end == -1) {
            newStreamLength = this.endPosition - newStreamStart;
        } else {
            newStreamLength = end - start;
        }

        return new GridFsSharedInputStream(gridFSDBFile, newStreamStart, newStreamLength);
    }

    @Override
    public int read() throws IOException {
        LOG.debug("read");
        byte[] b = new byte[1];

        int read = read(b);

        if (read < 0) {
            return -1;
        }

        return b[0] & 0xFF;
    }

    @Override
    public int read(byte[] b) throws IOException {
        LOG.debug("read(byte[])");
        return read(b , 0 , b.length);
    }

    @Override
    public int available() throws IOException {
        int available = this.inputStream.available();

        return (int)Math.min(this.endPosition - this.position, available);
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }



    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        LOG.debug("read(byte[], {}, {})", offset, length);
        if (b == null) {
            throw new NullPointerException();
        } else if (offset < 0 || length < 0 || length > b.length - offset) {
            throw new IndexOutOfBoundsException();
        }

        if (this.position >= this.endPosition) {
            return -1;
        }

        int bytesToRead = (int)Math.min(this.endPosition - this.position, length);

        int readBytes = this.inputStream.read(b, offset, bytesToRead);
        this.position += readBytes;

        return readBytes;
    }


}
