package eu.eyan.idakonyvtar.testutil.video;

import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.FormatKeys.MIME_AVI;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.FormatKeys.MimeTypeKey;
import static org.monte.media.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.VideoFormatKeys.DepthKey;
import static org.monte.media.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.VideoFormatKeys.QualityKey;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

import org.fest.swing.timing.Pause;
import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.math.Rational;

/**
 * Class to make it possible to record a component.<br>
 * It uses the monte screen recorder for that. (LGPL3)
 */
public class VideoRecorder {

	private ScreenRecorderToFile screenRecorder;

	public void start(Component component, String fileLocation, String videoName) {
		try {
			GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

			Format fileFormat = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI);

			Format screenFormat = new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, CompressorNameKey,
					ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24, FrameRateKey, Rational.valueOf(15), QualityKey, 1.0f, KeyFrameIntervalKey, 15 * 60);

			Format mouseFormat = new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black", FrameRateKey, Rational.valueOf(30));

			Rectangle capture = new Rectangle(component.getLocationOnScreen(), component.getSize());

			screenRecorder = new ScreenRecorderToFile(gc, capture, fileFormat, screenFormat, mouseFormat, null, new File(fileLocation), videoName);
			screenRecorder.start();
		} catch (IOException e) {
			// FIXME
			e.printStackTrace();
		} catch (AWTException e) {
			// FIXME
			e.printStackTrace();
		}

	}

	public void stopUndVideoSpeichern() {
		Pause.pause(3000);
		try {
			stop();
		} catch (IOException e) {
			e.printStackTrace();// FIXME
		}
	}

	public void stopUndVideoVerwerfen() {
		File videoFile = screenRecorder.getVideoFile();
		try {
			stop();
		} catch (IOException e) {
			e.printStackTrace();// FIXME
		} finally {
			if (videoFile != null && videoFile.exists() && videoFile.isFile()) {
				videoFile.delete();
			}
		}
	}

	private void stop() throws IOException {
		if (screenRecorder != null) {
			screenRecorder.stop();
			screenRecorder = null;
		}
	}
}