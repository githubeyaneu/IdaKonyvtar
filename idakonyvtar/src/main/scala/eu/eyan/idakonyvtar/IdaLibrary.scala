package eu.eyan.idakonyvtar

import com.google.common.io.Resources
import eu.eyan.idakonyvtar.controller.input.LibraryControllerInput
import eu.eyan.idakonyvtar.controller.LibraryController
import eu.eyan.idakonyvtar.util.DialogHelper
import java.io.File
import eu.eyan.log.Log
import eu.eyan.util.awt.AwtHelper._
import eu.eyan.log.LogWindow

object IdaLibrary {

  val DEFAULT_LIBRARY = "library.xls";
  val VERSION = "1.1.1"

  def main(args: Array[String]): Unit = {
    val pathname = if (args.isEmpty || args(0) == null) DEFAULT_LIBRARY; else args(0);

    val fileToOpen =
      if (!(new File(pathname)).exists())
        new File(Resources.getResource(pathname).getFile());
      else
        new File(pathname);

    Log.info("Resource -> File: " + fileToOpen);

    val frame = DialogHelper.runInFrameFullScreen(new LibraryController(), new LibraryControllerInput(fileToOpen), LibraryController.TITLE)
    frame.addWindowListener(newWindowClosingEvent(e => LogWindow.close()))
  }
}