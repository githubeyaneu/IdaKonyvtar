package eu.eyan.idakonyvtar

import java.awt.BorderLayout
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.net.URLEncoder

import eu.eyan.idakonyvtar.controller.LibraryController
import eu.eyan.idakonyvtar.controller.input.LibraryControllerInput
import eu.eyan.idakonyvtar.text.TextsIda
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.idakonyvtar.util.WebCam
import eu.eyan.log.Log
import eu.eyan.log.LogWindow
import eu.eyan.util.awt.clipboard.ClipboardPlus
import eu.eyan.util.registry.RegistryPlus
import eu.eyan.util.swing.Alert
import eu.eyan.util.swing.JFramePlus
import eu.eyan.util.swing.JFramePlus.JFramePlusImplicit
import eu.eyan.util.text.Text.emptySingularPlural
import javax.swing.JFrame
import eu.eyan.idakonyvtar.view.LibraryMenuAndToolBar

object IdaLibraryFrame {
  def apply(fileToOpen: File) = new IdaLibraryFrame().startLibrary(fileToOpen)
}

class IdaLibraryFrame private(){

  private val texts = new TextsIda

  private def selectLanguageByDialog(languages: Array[String]) = Alert.alertOptions("Language selection", "Please select your language!", languages)

  private def confirmExit(frame: JFrame) = DialogHelper.yesNo(frame, texts.ExitWindowConfirmQuestion, texts.ExitWindowTitle, texts.ExitWindowYes, texts.ExitWindowNo)

  private def getAllLogs = { Alert.alert(texts.CopyLogsWindowTitle, texts.CopyLogsWindowText, texts.CopyLogsWindowButton); Log.toString }

  private def showAbout: Unit = Alert.alert(texts.AboutWindowTitle, texts.AboutWindowText, texts.AboutWindowButton)

  private def writeEmail = Desktop.getDesktop.mail(new URI("mailto:idalibrary@eyan.hu?subject=IdaLibrary%20error&body=" + URLEncoder.encode(getAllLogs, "utf-8").replace("\\+", "%20")))

  def startLibrary(fileToOpen: File) = {
    if (texts.initialLanguage.isEmpty) selectLanguageByDialog(texts.languages).foreach(texts.onLanguageSelected)

    val controller = new LibraryController
    controller.initData(new LibraryControllerInput(fileToOpen))
    val view = controller.getView
    controller.initBindings // TODO has to be after get view (set selection model for list)
    def loadLibrary:Unit = controller.loadLibrary
    def saveLibrary:Unit = controller.saveLibrary
    def numberOfBooks = controller.numberOfBooks
    def initialComponentToFocus = controller.getComponentForFocus
    

    // TODO: remove toolbar from controller
    val toolBar = controller.menuAndToolBar.getToolBar
    val toolBar_ = new LibraryMenuAndToolBar().getToolBar

    new JFrame()
      .name(classOf[IdaLibrary].getName)
      .title(emptySingularPlural(numberOfBooks, texts.IdaLibraryTitleEmpty, texts.IdaLibraryTitleSingular, texts.IdaLibraryTitlePlural(numberOfBooks)))
      .iconFromChar('I')
      .addFluent(toolBar, BorderLayout.NORTH)
      .addFluent(view, BorderLayout.CENTER)
      .menuItem(texts.MenuFile, texts.MenuFileLoad, loadLibrary)
      .menuItem(texts.MenuFile, texts.MenuFileSave, saveLibrary)
      .menuItemSeparator(texts.MenuFile)
      .menuItemEvent(texts.MenuFile, texts.MenuFileExit, JFramePlus.close)
      .menuItems(texts.MenuLanguages, texts.languages, texts.onLanguageSelected: String => Unit)
      .menuItemEvent(texts.MenuDebug, texts.MenuDebugLogWindow, LogWindow.show)
      .menuItem(texts.MenuDebug, texts.MenuDebugCopyLogs, ClipboardPlus.copyToClipboard(getAllLogs))
      .menuItem(texts.MenuDebug, texts.MenuDebugClearRegistry, RegistryPlus.clear(classOf[IdaLibrary].getName))
      .menuItem(texts.MenuHelp, texts.MenuHelpEmailError, writeEmail)
      .menuItem(texts.MenuHelp, texts.MenuHelpAbout, showAbout)
      .onCloseDisposeWithCondition(confirmExit)
      .onWindowClosed({ LogWindow.close; WebCam.stop })
      .packAndSetVisible
      .positionToCenter
      .maximize
      .focusComponentInWindow(initialComponentToFocus)
  }
}