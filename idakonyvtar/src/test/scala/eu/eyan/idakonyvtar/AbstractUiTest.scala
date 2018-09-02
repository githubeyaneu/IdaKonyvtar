package eu.eyan.idakonyvtar

import java.util.concurrent.TimeUnit

import org.fest.swing.core.EmergencyAbortListener
import org.fest.swing.timing.Pause
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import eu.eyan.testutil.video.VideoRunner

object AbstractUiTest {

  @BeforeClass
  def setUpClass(): Unit = {
    EmergencyAbortListener.registerInToolkit()
  }

}

@RunWith(classOf[VideoRunner])
abstract class AbstractUiTest {

  val globalTimeout_ : Timeout = new Timeout(60, TimeUnit.SECONDS)

  @Rule
  def globalTimeout = globalTimeout_

  protected def pause(ms: Long): Unit = {
    Pause.pause(ms)
  }

}
