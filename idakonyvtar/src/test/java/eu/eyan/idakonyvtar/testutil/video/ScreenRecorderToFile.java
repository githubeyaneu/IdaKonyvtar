package eu.eyan.idakonyvtar.testutil.video;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.screenrecorder.ScreenRecorder;

/**
 * Class that makes possible to save video files into the specified directory.
 */
class ScreenRecorderToFile extends ScreenRecorder {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmmss");
	private final File videoFile;

	public ScreenRecorderToFile(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat, Format screenFormat, Format mouseFormat,
			Format audioFormat, File folder, String videoName) throws IOException, AWTException {
		super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, folder);

		if (!folder.exists()) {
			folder.mkdirs();
		} else if (!folder.isDirectory()) {
			throw new IOException("\"" + folder + "\" is not a directory.");
		}

		String timeString = ScreenRecorderToFile.dateFormat.format(new Date());
		String extension = Registry.getInstance().getExtension(fileFormat);
		videoFile = new File(folder, videoName + "_" + timeString + "." + extension);
	}

	@Override
	protected File createMovieFile(Format fileFormat) throws IOException {
		return videoFile;
	}

	public File getVideoFile() {
		return videoFile;
	}
}