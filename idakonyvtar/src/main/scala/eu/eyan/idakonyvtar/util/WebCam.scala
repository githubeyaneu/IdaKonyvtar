package eu.eyan.idakonyvtar.util

import java.lang.Thread.UncaughtExceptionHandler

import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamPanel

import eu.eyan.log.Log
import javax.swing.JComboBox
import com.github.sarxos.webcam.WebcamPickerCellRenderer
import java.util.concurrent.TimeUnit
import scala.collection.JavaConversions._
import java.awt.Dimension

case class WebCamStartResult(panel: WebcamPanel, thread: Thread, picker: JComboBox[Webcam])

object WebCam {
	var webcam: Option[WebCamStartResult] = None
	
  def getImage = {
	  if (webcam.nonEmpty) {
    val  image = webcam.get.picker.getSelectedItem.asInstanceOf[Webcam].getImage
    image
	  } else null
  }

  def startWebcam = {
    if (webcam == None) {
      //    with WebcamListener
      //    with WindowListener
      //    with ItemListener
      //    with WebcamDiscoveryListener

      val webcams = Webcam.getWebcams.toList.toArray
      val picker = new JComboBox[Webcam](webcams)
      def getSelectedWebcam = picker.getSelectedItem.asInstanceOf[Webcam]

      if (getSelectedWebcam == null) {
        None
      } else {
        getSelectedWebcam.setViewSize(getSelectedWebcam.getViewSizes.last)
        val panel = new WebcamPanel(getSelectedWebcam, new Dimension(320,240), false)

        val t: Thread = new Thread { override def run = panel.start }
        t.setName("example-starter")
        t.setDaemon(true)
        t.setUncaughtExceptionHandler(new UncaughtExceptionHandler {
          def uncaughtException(x: Thread, x2: Throwable) = Log.error(s"webcam thread error $x", x2)
        })
        t.start
        Log.info("start webcam success")
        webcam = Option(WebCamStartResult(panel, t, picker))
      }
    }

    webcam
  }

  def stop = {
    if (webcam.nonEmpty) {
      Log.info("stop webcam")
      webcam.get.panel.stop
      webcam.get.thread.stop
      webcam=None
    }
  }

}