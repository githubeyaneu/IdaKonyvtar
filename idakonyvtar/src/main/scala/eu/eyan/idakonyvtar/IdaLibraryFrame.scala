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
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import eu.eyan.util.swing.SpecialCharacterRowFilter

object IdaLibraryFrame {
  def apply(fileToOpen: File) = new IdaLibraryFrame().startLibrary(fileToOpen)
}

class IdaLibraryFrame private () {

  private val texts = new TextsIda

  private def selectLanguageByDialog(languages: Array[String]) = Alert.alertOptions("Language selection", "Please select your language!", languages)

  private def confirmExit(frame: JFrame) = DialogHelper.yesNo(frame, texts.ExitWindowConfirmQuestion, texts.ExitWindowTitle, texts.ExitWindowYes, texts.ExitWindowNo)

  private def getAllLogs = { Alert.alert(texts.CopyLogsWindowTitle, texts.CopyLogsWindowText, texts.CopyLogsWindowButton); Log.toString }

  private def showAbout: Unit = Alert.alert(texts.AboutWindowTitle, texts.AboutWindowText, texts.AboutWindowButton)

  private def writeEmail = Desktop.getDesktop.mail(new URI("mailto:idalibrary@eyan.hu?subject=IdaLibrary%20error&body=" + URLEncoder.encode(getAllLogs, "utf-8").replace("\\+", "%20")))

  def startLibrary(fileToOpen: File) = {
    if (texts.initialLanguage.isEmpty) selectLanguageByDialog(texts.languages).foreach(texts.onLanguageSelected)

    // TODO: remove toolbar from controller
    //    val toolBar_ = controller.menuAndToolBar.getToolBar
    val menuAndToolBar = new LibraryMenuAndToolBar()
    val toolBar = menuAndToolBar.getToolBar
    def initialComponentToFocus = menuAndToolBar.TOOLBAR_SEARCH

    val controller = new LibraryController
    controller.initData(new LibraryControllerInput(fileToOpen))
    val view = controller.getView
    
    controller.initBindings // TODO has to be after get view (set selection model for list)
    menuAndToolBar.TOOLBAR_LOAD.onAction(controller.loadLibrary)
    menuAndToolBar.TOOLBAR_SAVE.onAction(controller.saveLibrary)
    menuAndToolBar.TOOLBAR_NEW_BOOK.onAction(controller.createNewBook)
    menuAndToolBar.TOOLBAR_BOOK_DELETE.onAction(controller.deleteBook(menuAndToolBar.TOOLBAR_BOOK_DELETE))
    controller.view.getBookTable.getSelectionModel.addListSelectionListener(new ListSelectionListener() {
      def valueChanged(e: ListSelectionEvent) = {
        menuAndToolBar.TOOLBAR_BOOK_DELETE.setEnabled(controller.view.getBookTable().getSelectedRow >= 0)
      }
    })
    menuAndToolBar.TOOLBAR_SEARCH.addKeyListener(new KeyAdapter() {
      override def keyReleased(e: KeyEvent) = {
        controller.view.getBookTable().setRowFilter(new SpecialCharacterRowFilter(menuAndToolBar.TOOLBAR_SEARCH.getText()))
        controller.highlightRenderer.setHighlightText(menuAndToolBar.TOOLBAR_SEARCH.getText())
      }
    })

    def loadLibrary: Unit = controller.loadLibrary
    def saveLibrary: Unit = controller.saveLibrary
    def numberOfBooks = controller.numberOfBooks

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