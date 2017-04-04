package eu.eyan.idakonyvtar

import java.io.File

import com.google.common.io.Resources

import eu.eyan.idakonyvtar.controller.LibraryController
import eu.eyan.idakonyvtar.controller.input.LibraryControllerInput
import eu.eyan.idakonyvtar.text.Texts
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.log.Log
import eu.eyan.log.LogWindow
import eu.eyan.util.awt.AwtHelper.newWindowClosingEvent

object IdaLibrary {

  val DEFAULT_LIBRARY = "library.xls"
  val VERSION = "1.1.1"

  def main(args: Array[String]): Unit = {
    val path = if (args.isEmpty || args(0) == null) DEFAULT_LIBRARY; else args(0)

    val file = new File(path)

    val fileToOpen =
      if (file.exists()) file
      else new File(Resources.getResource(path).getFile)

    Log.activate
    Log.info("Resource -> File: " + fileToOpen)

    val frame = DialogHelper.runInFrameFullScreen(new LibraryController(), new LibraryControllerInput(fileToOpen), Texts.TITLE)
    frame.addWindowListener(newWindowClosingEvent(e => LogWindow.close()))
  }
}