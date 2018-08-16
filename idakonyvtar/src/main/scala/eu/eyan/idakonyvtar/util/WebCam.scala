package eu.eyan.idakonyvtar.util

import com.github.sarxos.webcam.Webcam

object WebCam {
  def getImage = {
    val webcam = Webcam.getDefault
    webcam.setViewSize(webcam.getViewSizes.last)
    webcam.open
    val image = webcam.getImage
    webcam.close
    image
  }
}