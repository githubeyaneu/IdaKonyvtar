package eu.eyan.idakonyvtar;

import org.fest.swing.core.EmergencyAbortListener;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.Timeout;

public class AbstractUiTest
{
    @Rule
    public Timeout globalTimeout = new Timeout(10000);

    @BeforeClass
    public static void setUpClass()
    {
        EmergencyAbortListener.registerInToolkit();
    }
}
