package eu.eyan.idakonyvtar.util;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingUtilities;

import org.fest.swing.core.BasicRobot;
import org.fest.swing.timing.Pause;
import org.junit.AssumptionViolatedException;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.Registry;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

/**
 * Usage: try { TestScreenRecorder.start(
 * AbstractHelper.getFrameFixture().target,
 * JusFailureScreenshotTaker.FAILED_GUI_TESTS_FOLDER, String.format("%02d",
 * (JusFailureScreenshotTaker.getScreenshotCounter() + 1)) + "_" +
 * thisKlasse.getSimpleName() + "-" + "test");
 * 
 * ...executeTest TestScreenRecorder.stopUndVideoVerwerfen(); } catch (Throwable
 * t) { TestScreenRecorder.stopUndVideoSpeichern(); throw t; }
 */
public class TestVideoRecorder {

	public static class VideoRunner extends BlockJUnit4ClassRunner {

		public VideoRunner(Class<?> klass) throws InitializationError {
			super(klass);
		}

		/**
		 * Returns a {@link Statement} that invokes {@code method} on
		 * {@code test}
		 */
		@Override
		protected Statement methodInvoker(FrameworkMethod method, Object test) {
			final String testName = test.getClass().getSimpleName() + '.'
					+ method.getName();
			return new InvokeMethod(method, test) {
				@Override
				public void evaluate() throws Throwable {
					if (componentToRecord == null) {
						throw new Exception(
								"Window for Screen recording not found. Please set it in the @Before method for the test: "
										+ testName
										+ "\r\nFor Example: "
										+ TestVideoRecorder.class
												.getSimpleName()
										+ ".setComponentToRecord(component);");
					}
					try {
						TestVideoRecorder.start(componentToRecord,
								"FailedTestVideos", testVideoCounter + "_"
										+ testName);
						super.evaluate();
						TestVideoRecorder.stopUndVideoVerwerfen();
					} catch (Throwable t) {
						TestVideoRecorder.stopUndVideoSpeichern();
						testVideoCounter++;
						throw t;
					} finally {
						componentToRecord = null;
					}
				}
			};
		}
	}

	private static ScreenRecorder screenRecorder;
	private static String videoName;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyyMMdd HHmmss");
	private static File videoFile;

	private static Component componentToRecord = null;
	private static int testVideoCounter = 1;

	public static void setComponentToRecord(Component componentToRecord) {
		TestVideoRecorder.componentToRecord = componentToRecord;
	}

	private static class ScreenRecorderToFile extends ScreenRecorder {

		public ScreenRecorderToFile(GraphicsConfiguration cfg,
				Rectangle captureArea, Format fileFormat, Format screenFormat,
				Format mouseFormat, Format audioFormat, File movieFolder,
				String name) throws IOException, AWTException {
			super(cfg, captureArea, fileFormat, screenFormat, mouseFormat,
					audioFormat, movieFolder);
			videoName = name;
		}

		@Override
		protected File createMovieFile(Format fileFormat) throws IOException {
			if (!movieFolder.exists()) {
				movieFolder.mkdirs();
			} else if (!movieFolder.isDirectory()) {
				throw new IOException("\"" + movieFolder
						+ "\" is not a directory.");
			}

			videoFile = new File(movieFolder, videoName + "_"
					+ dateFormat.format(new Date()) + "."
					+ Registry.getInstance().getExtension(fileFormat));
			return videoFile;
		}

	}

	public static void start(Component component, String fileLocation,
			String videoName) {
		try {
			GraphicsConfiguration gc = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDefaultConfiguration();
			Format fileFormat = new Format(MediaTypeKey, MediaType.FILE,
					MimeTypeKey, MIME_AVI);
			Format screenFormat = new Format(MediaTypeKey, MediaType.VIDEO,
					EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
					CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
					DepthKey, 24, FrameRateKey, Rational.valueOf(15),
					QualityKey, 1.0f, KeyFrameIntervalKey, 15 * 60);
			Format mouseFormat = new Format(MediaTypeKey, MediaType.VIDEO,
					EncodingKey, "black", FrameRateKey, Rational.valueOf(30));

			Rectangle capture = new Rectangle(component.getLocationOnScreen(),
					component.getSize());

			screenRecorder = new ScreenRecorderToFile(gc, capture, fileFormat,
					screenFormat, mouseFormat, null, new File(fileLocation),
					videoName);
			screenRecorder.start();
		} catch (IOException e) {
			// FIXME
			e.printStackTrace();
		} catch (AWTException e) {
			// FIXME
			e.printStackTrace();
		}

	}

	public static void stopUndVideoSpeichern() {
		Pause.pause(3000);
		try {
			stop();
		} catch (IOException e) {
			e.printStackTrace();// FIXME
		}
	}

	public static void stopUndVideoVerwerfen() {
		try {
			stop();
			if (videoFile != null && videoFile.exists() && videoFile.isFile()) {
				videoFile.delete();
			}
		} catch (IOException e) {
			e.printStackTrace();// FIXME
		}
	}

	private static void stop() throws IOException {
		if (screenRecorder != null) {
			screenRecorder.stop();
			screenRecorder = null;
		}
	}
}