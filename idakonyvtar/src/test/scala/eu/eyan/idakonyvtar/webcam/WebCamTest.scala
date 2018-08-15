package eu.eyan.idakonyvtar.webcam

import org.junit.runner.RunWith
import org.junit.Test
import eu.eyan.testutil.ScalaEclipseJunitRunner
import com.github.sarxos.webcam.Webcam
import javax.imageio.ImageIO
import java.io.File
import java.awt.Dimension
import com.github.sarxos.webcam.WebcamResolution
import com.github.sarxos.webcam.WebcamPanel
import javax.swing.JFrame

@RunWith(classOf[ScalaEclipseJunitRunner])
class WebCamTest {
  @Test
  def takePhoto = {
    //https://github.com/sarxos/webcam-capture
    val webcam = Webcam.getDefault
    webcam.setViewSize(webcam.getViewSizes.last)
    webcam.open
    val image = webcam.getImage
    //ImageIO.write(image, "JPG", new File("""C:\Users\anfr895\Desktop\test.JPG"""));
    webcam.close
  }

  @Test
  def takeVideo = {
  }
}
object WebCamTest extends App {

  val webcam = Webcam.getDefault
  webcam.setViewSize(WebcamResolution.VGA.getSize())

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