package eu.eyan.idakonyvtar.webcam

import com.github.sarxos.webcam.{WebcamPanel, WebcamPicker, WebcamResolution}
import eu.eyan.log.Log
import eu.eyan.util.swing.JFramePlus.JFramePlusImplicit
import javax.swing.JFrame

object WebcamViewerExample extends App {

  case class WebCamStartResult(panel:WebcamPanel, thread:Thread)
  def startWebcam = {
    //    with WebcamListener
    //    with WindowListener
    //    with ItemListener
    //    with WebcamDiscoveryListener

    val picker = new WebcamPicker()
    val webcam = picker.getSelectedWebcam
    if (webcam == null) {
      None
    } else {
      webcam.setViewSize(WebcamResolution.QQVGA.getSize)
      val panel = new WebcamPanel(webcam, false)

      val t: Thread = new Thread() {
        override def run(): Unit = {
          panel.start()
        }
      }
      t.setName("example-starter")
      t.setDaemon(true)
      t.setUncaughtExceptionHandler((x: Thread, x2: Throwable) => Log.error(s"webcam thread error $x", x2))
      t.start()
      Option( WebCamStartResult(panel, t))
    }
  }

  new JFrame().title("Java Webcam Capture POC").onCloseExit.withComponent(startWebcam.get.panel).packAndSetVisible

}
