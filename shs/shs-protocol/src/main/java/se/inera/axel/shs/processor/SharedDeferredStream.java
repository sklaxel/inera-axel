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
package se.inera.axel.shs.processor;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.SharedInputStream;
import javax.mail.util.SharedByteArrayInputStream;
import javax.mail.util.SharedFileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SharedDeferredStream {
    private static Logger log = LoggerFactory.getLogger(SharedDeferredStream.class);

    private static File tempDir;

    /**
     * Size limit at which the method {@link #createDeferredOutputStream()} overflows
     * to a temporary file and returns an input stream of that file, instead of an input stream of a byte buffer.
     */
    public static final int DEFAULT_OVERFLOW_TO_DISK_BYTES = 16 * (int) FileUtils.ONE_MB;

    private SharedDeferredStream() {}

    {
        tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
    }

    public static DeferredFileOutputStream createDeferredOutputStream() {

        DeferredFileOutputStream outputStream =
                new DeferredFileOutputStream(DEFAULT_OVERFLOW_TO_DISK_BYTES,
                        "axel-", ".tmp", tempDir);
        return outputStream;
    }

    public static InputStream toSharedInputStream(DeferredFileOutputStream outputStream)
        throws IOException
    {
        if (outputStream.isInMemory()) {
  			if (log.isDebugEnabled())
  				log.debug("written to memory");

  			return new SharedByteArrayInputStream(outputStream.getData());
  		} else {
  			if (log.isDebugEnabled())
  				log.debug("written to file: " + outputStream.getFile());

            // Schedule the file for deletion in case the delete on close of the
            // SharedTemporaryFileInputStream does not work
            File outputFile = outputStream.getFile();
            if (outputFile != null) {
                outputFile.deleteOnExit();
            }

  			return new SharedTemporaryFileInputStream(outputStream.getFile());
  		}
    }


    public static InputStream toSharedInputStream(InputStream inputStream)
    throws IOException
    {

        if (inputStream instanceof SharedInputStream) {
            return inputStream;
        }

        DeferredFileOutputStream outputStream = createDeferredOutputStream();

        try {
            IOUtils.copyLarge(inputStream, outputStream);
            return SharedDeferredStream.toSharedInputStream(outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * A <code>SharedFileInputStream</code> that deletes the underlying file when the
     * stream is closed.
     */
    private static class SharedTemporaryFileInputStream extends SharedFileInputStream {
        private File temporaryFile;

        public SharedTemporaryFileInputStream(File temporaryFile) throws IOException {
            super(temporaryFile);
            this.temporaryFile = temporaryFile;
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (in == null && this.temporaryFile != null) {
                this.temporaryFile.delete();
            }
        }
    }

}
