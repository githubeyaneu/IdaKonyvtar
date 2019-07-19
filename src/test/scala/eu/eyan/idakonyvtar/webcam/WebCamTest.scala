package eu.eyan.idakonyvtar.webcam

import com.github.sarxos.webcam.{Webcam, WebcamPanel, WebcamResolution}
import eu.eyan.testutil.{ScalaEclipseJunitRunner, TestPlus}
import javax.swing.JFrame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(classOf[ScalaEclipseJunitRunner])
class WebCamTest extends TestPlus{
  
//  @Test
  def takePhoto = {
    //https://github.com/sarxos/webcam-capture
    val webcam = Webcam.getDefault
    waitFor(webcam.isOpen ==> false, 1000)
    webcam.setViewSize(webcam.getViewSizes.last)
    webcam.open
//    val image = webcam.getImage
    //ImageIO.write(image, "JPG", new File("""C:\Users\anfr895\Desktop\test.JPG"""));
    webcam.close
  }

  @Test
  def takeVideo = {
  }
}
object WebCamTest extends App {

  val webcam = Webcam.getDefault
  webcam.setViewSize(WebcamResolution.VGA.getSize)

  val panel = new WebcamPanel(webcam)
  panel.setFPSDisplayed(true)
  panel.setDisplayDebugInfo(true)
  panel.setImageSizeDisplayed(true)
  panel.setMirrored(true)

  val window = new JFrame("Test webcam panel")
  window.add(panel)
  window.setResizable(true)
  window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  window.pack()
  window.setVisible(true)
  webcam.close
}