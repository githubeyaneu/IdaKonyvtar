package eu.eyan.idakonyvtar;

import java.util.concurrent.TimeUnit;

import org.fest.swing.core.EmergencyAbortListener;
import org.fest.swing.timing.Pause;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.Timeout;

public class AbstractUiTest {
	@Rule
	public Timeout globalTimeout = new Timeout(60, TimeUnit.SECONDS);

	@BeforeClass
	public static void setUpClass() {
		EmergencyAbortListener.registerInToolkit();
	}

	protected void pause(long ms) {
		Pause.pause(ms);
	}
}