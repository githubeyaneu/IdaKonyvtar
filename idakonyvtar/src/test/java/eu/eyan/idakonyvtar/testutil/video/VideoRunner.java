package eu.eyan.idakonyvtar.testutil.video;

import java.awt.Component;

import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * JUnit {@link Runner} class to make it possible to record video from the
 * tests.<br>
 * <br>
 * It was not possible to do this as a JUnit Rule (Test or Method), because the
 * rule applies before the @Before method.<br>
 * It might be needed to set the component which has to recorded which may
 * happen in the @Before method.<br>
 * <br>
 * Anyway this Runner is the same as BlockJUnit4ClassRunner with the video
 * recording.
 * 
 * TODO: extend with the possibility to record the whole screen.
 */
public class VideoRunner extends BlockJUnit4ClassRunner {

	private static int testVideoCounter = 1;

	public VideoRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	static Component componentToRecord = null;

	public static void setComponentToRecord(Component componentToRecord) {
		VideoRunner.componentToRecord = componentToRecord;
	}

	@Override
	protected Statement methodInvoker(FrameworkMethod method, Object test) {
		final String testName = test.getClass().getSimpleName() + '.' + method.getName();

		return new InvokeMethod(method, test) {
			@Override
			public void evaluate() throws Throwable {
				if (componentToRecord == null) {
					throw new Exception("Component to record not found. Please set it in the @Before method for the test: " + testName + "\r\nFor Example: "
							+ VideoRunner.class.getSimpleName() + ".setComponentToRecord(component);");
				}
				VideoRecorder videoRecorder = new VideoRecorder();
				try {
					videoRecorder.start(componentToRecord, "FailedTestVideos", testVideoCounter + "_" + testName);
					super.evaluate();
					videoRecorder.stopUndVideoVerwerfen();
				} catch (Throwable t) {
					videoRecorder.stopUndVideoSpeichern();
					testVideoCounter++;
					throw t;
				} finally {
					componentToRecord = null;
				}
			}
		};
	}
}