package eu.eyan.idakonyvtar

import java.io.File

import com.google.common.io.Resources

import eu.eyan.idakonyvtar.controller.LibraryController
import eu.eyan.idakonyvtar.controller.input.LibraryControllerInput
import eu.eyan.idakonyvtar.text.Texts
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.log.Log
import eu.eyan.log.LogWindow
<<<<<<< HEAD
import eu.eyan.util.awt.AwtHelper.newWindowClosingEvent
=======
import eu.eyan.util.awt.AwtHelper.onWindowClosing
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git

object IdaLibrary {

  val DEFAULT_LIBRARY = "library.xls"
  val VERSION = "1.1.1"

  def main(args: Array[String]): Unit = {
    val path = if (args.isEmpty || args(0) == null) DEFAULT_LIBRARY; else args(0)

    val file = new File(path)

    val fileToOpen =
      if (file.exists()) file
      else new File(Resources.getResource(path).getFile)

<<<<<<< HEAD
    Log.activate()
=======
    Log.activate
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
    Log.info("Resource -> File: " + fileToOpen)

    val frame = DialogHelper.runInFrameFullScreen(new LibraryController(), new LibraryControllerInput(fileToOpen), Texts.TITLE)
<<<<<<< HEAD
    frame.addWindowListener(newWindowClosingEvent(e => LogWindow.close()))
=======
    frame.addWindowListener(onWindowClosing(e => LogWindow.close))
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
  }
}