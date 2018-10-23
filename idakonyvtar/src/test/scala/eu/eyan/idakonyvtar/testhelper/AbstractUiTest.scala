package eu.eyan.idakonyvtar.testhelper

import java.util.concurrent.TimeUnit
import org.fest.swing.core.EmergencyAbortListener
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import eu.eyan.testutil.video.VideoRunner
import eu.eyan.testutil.TestPlus
import org.junit.Before

object AbstractUiTest {
  @BeforeClass def setUpClass = EmergencyAbortListener.registerInToolkit
}

@RunWith(classOf[VideoRunner])
abstract class AbstractUiTest extends TestPlus {
  def timeout = 30
  
  @Before def setUpAbstractUiTest = EmergencyAbortListener.registerInToolkit // TODO do a system exit also (wont execute any more test)
  
  val globalTimeout_ : Timeout = new Timeout(timeout, TimeUnit.SECONDS)
  @Rule def globalTimeout = globalTimeout_
}