package se.inera.axel.test.fitnesse.fixtures;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class ShsCmdlineDriver {

	private final static Logger log = LoggerFactory
			.getLogger(ShsCmdlineDriver.class);

	public String echo(String input) {
		return input;
	}

	/**
	 * Creates a temporary file with file size in bytes.
	 * 
	 * @param size
	 * @return
	 * @throws IOException
	 */
	public String createFileWithSizeInBytes(int size) throws IOException {
		InputStream inputStream = new NullInputStream(size);

		File file = File.createTempFile("FitNesse_", ".bin");
		FileOutputStream fileOutputStream = new FileOutputStream(
				file.getAbsoluteFile());
		IOUtils.copyLarge(inputStream, fileOutputStream);
		log.info("File " + file.getAbsolutePath() + " created");

		return file.getAbsolutePath();
	}

	/**
	 * Removes a file with absolute file path.
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean removeFile(String fileName) {
		File file = new File(fileName);
		boolean isDeleted = file.delete();
		log.info("File " + file.getAbsolutePath() + " deleted? " + isDeleted);

		return isDeleted;
	}
}
