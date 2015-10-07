package eu.eyan.idakonyvtar;

import java.util.concurrent.TimeUnit;

import org.fest.swing.core.EmergencyAbortListener;
import org.fest.swing.timing.Pause;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.eyan.idakonyvtar.util.TestVideoRecorder;
import eu.eyan.idakonyvtar.util.TestVideoRecorder.VideoRunner;

@RunWith(VideoRunner.class)
public class AbstractUiTest {

	@SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "Testrule")
	@Rule
	public Timeout globalTimeout = new Timeout(60, TimeUnit.SECONDS);

	// @Rule
	// public TestVideoRecorder testVideoRecorder = new TestVideoRecorder();

	@BeforeClass
	public static void setUpClass() {
		EmergencyAbortListener.registerInToolkit();
	}

	protected void pause(long ms) {
		Pause.pause(ms);
	}
}