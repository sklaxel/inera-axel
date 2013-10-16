package se.inera.axel.shs.broker.messagestore.internal;

import com.mongodb.gridfs.GridFSDBFile;
import org.apache.commons.io.IOUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class GridFsSharedInputStreamTest {
    private GridFSDBFile gridFSDBFile;

    @BeforeMethod
    public void setUp() throws Exception {
        final byte[] filebytes = stringToBytesASCII("abcdefghi");

        gridFSDBFile = mock(GridFSDBFile.class);
        when(gridFSDBFile.getInputStream()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new ByteArrayInputStream(filebytes);
            }
        });

        when(gridFSDBFile.getLength()).thenReturn(9L);
    }

    public static byte[] stringToBytesASCII(String str) {
        char[] buffer = str.toCharArray();
        byte[] b = new byte[buffer.length];

        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) buffer[i];
        }

        return b;
    }

    @Test
    public void readCompleteStreamShouldReturnAllBytes() throws IOException {
        GridFsSharedInputStream inputStream = new GridFsSharedInputStream(gridFSDBFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        IOUtils.copy(inputStream, baos);

        assertEquals(baos.toByteArray(), stringToBytesASCII("abcdefghi"));
    }

    @Test
    public void newStreamShouldReturnASubsetOfTheData() throws IOException {
        GridFsSharedInputStream inputStream = new GridFsSharedInputStream(gridFSDBFile);
        InputStream subsetStream = inputStream.newStream(2, 6);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        IOUtils.copy(subsetStream, baos);

        assertEquals(baos.toByteArray(), stringToBytesASCII("cdef"));
    }

    @Test
    public void whenEndIsMinusOneNewStreamEndShouldBeTheSameAsTheUnderlyingStream() throws IOException {
        GridFsSharedInputStream inputStream = new GridFsSharedInputStream(gridFSDBFile);
        InputStream subsetStream = inputStream.newStream(2, -1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        IOUtils.copy(subsetStream, baos);

        assertEquals(baos.toByteArray(), stringToBytesASCII("cdefghi"));
    }

    @Test
    public void whenStreamIsResetItShouldContinueAtMarkPosition() throws IOException {
        GridFsSharedInputStream inputStream = new GridFsSharedInputStream(gridFSDBFile);
        byte[] part1 = new byte[4];
        IOUtils.read(inputStream, part1);
        assertEquals(part1, stringToBytesASCII("abcd"));

        inputStream.mark(10);
        byte[] part2 = new byte[3];
        IOUtils.read(inputStream, part2);
        assertEquals(part2, stringToBytesASCII("efg"));

        inputStream.reset();
        byte[] part3 = new byte[4];
        IOUtils.read(inputStream, part3);
        assertEquals(part3, stringToBytesASCII("efgh"));
    }

    @Test
    public void markShouldBeSupported() {
        GridFsSharedInputStream inputStream = new GridFsSharedInputStream(gridFSDBFile);
        assertTrue(inputStream.markSupported(), "Mark should be supported");
    }

    @Test
    public void readStreamByteByByteToTheEnd() throws IOException {
        GridFsSharedInputStream inputStream = new GridFsSharedInputStream(gridFSDBFile);
        int i;
        StringBuilder result = new StringBuilder();
        while((i=inputStream.read()) != -1)
        {
            // converts integer to character
            char c=(char)i;

            result.append(c);
        }

        assertEquals(result.toString(), "abcdefghi");
    }

    @Test
    public void getPositionShouldReturnTheCurrentPositionInTheStream() throws IOException {
        GridFsSharedInputStream inputStream = new GridFsSharedInputStream(gridFSDBFile);
        byte[] part1 = new byte[4];
        IOUtils.read(inputStream, part1);
        assertEquals(inputStream.getPosition(), 4);
    }

    @Test
    public void readWithByteBufferShouldReturnBytesRead() throws IOException {
        byte[] result = new byte[9];
        GridFsSharedInputStream inputStream = new GridFsSharedInputStream(gridFSDBFile);

        int bytesRead = inputStream.read(result, 0, 4);
        assertEquals(bytesRead, 4, "Should have read first 4 bytes");

        bytesRead = inputStream.read(result, 4, 1);
        assertEquals(bytesRead, 1, "Should have read fifth byte");

        bytesRead = inputStream.read(result, 5, 2);
        assertEquals(bytesRead, 2, "Should have read bytes 6 and 7");

        bytesRead = inputStream.read(result, 7, 2);
        assertEquals(bytesRead, 2, "Should have read the rest of the bytes");

        bytesRead = inputStream.read(new byte[3], 0, 3);
        assertEquals(bytesRead, -1, "Should have reached end of stream");

        assertEquals(result, stringToBytesASCII("abcdefghi"));
    }

    @Test
    public void shouldNotReadPastEndOfSubsetStream() throws IOException {
        GridFsSharedInputStream inputStream = new GridFsSharedInputStream(gridFSDBFile);
        InputStream subsetStream = inputStream.newStream(3, 4);

        byte[] part1 = new byte[100];
        int bytesRead = subsetStream.read(part1, 0, 100);

        assertEquals(bytesRead, 1, "Should not read past end of subset stream");
    }

    @Test
    public void nestedNewStreamCallsShouldAddTheOffset() throws IOException {
        // cdefg
        GridFsSharedInputStream inputStream = new GridFsSharedInputStream(gridFSDBFile, 2, 5);

        // ef
        InputStream subStream = inputStream.newStream(2, 4);

        byte[] result = new byte[9];

        int bytesRead = subStream.read(result);
        assertEquals(bytesRead, 2, "Should have read the two bytes of the nested stream");

        byte[] readBytes = Arrays.copyOf(result, bytesRead);

        assertEquals(readBytes, stringToBytesASCII("ef"));
    }
}
