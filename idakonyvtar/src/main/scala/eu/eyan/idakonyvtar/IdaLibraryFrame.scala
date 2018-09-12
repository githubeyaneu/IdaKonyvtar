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
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import eu.eyan.util.swing.SpecialCharacterRowFilter
import eu.eyan.util.swing.JTextFieldPlus.JTextFieldPlusImplicit
import eu.eyan.util.swing.JToolBarPlus.JToolBarImplicit
import javax.swing.JToolBar
import java.awt.Component
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit
import javax.swing.JTextField

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

    val controller = new LibraryController(fileToOpen)

    val jToolBar = new JToolBar("Alapfunkciók")
    jToolBar.addButton(texts.ToolbarSaveButton, "Library mentése", texts.ToolbarSaveButtonTooltip, texts.ToolbarSaveButtonIcon).onAction(controller.saveLibrary)
    jToolBar.addButton(texts.ToolbarLoadButton, "Library betöltése", texts.ToolbarLoadButtonTooltip, texts.ToolbarLoadButtonIcon).onAction(controller.loadLibrary)
    jToolBar.addButton(texts.ToolbarNewBookButton, "Új book hozzáadása", texts.ToolbarNewBookButtonTooltip, texts.ToolbarNewBookButtonIcon).onAction(controller.createNewBook)
    jToolBar.addButton(texts.ToolbarDeleteBookButton, "Book törlése", texts.ToolbarDeleteBookButtonTooltip, texts.ToolbarDeleteBookButtonIcon)
      .onActionPerformedEvent(evt => controller.deleteBook(evt.getSource.asInstanceOf[Component]))
      .enabled(controller.isBookSelected)
    jToolBar.addLabel(texts.ToolbarFilterLabel)
    val filterTextField = jToolBar.addTextField(5, "", "filter").widthSet(200).onTextChanged(controller.filter _)

    def loadLibrary: Unit = controller.loadLibrary
    def saveLibrary: Unit = controller.saveLibrary
    def numberOfBooks = controller.numberOfBooks
    def initialComponentToFocus = filterTextField

    new JFrame()
      .name(classOf[IdaLibrary].getName)
      .title(emptySingularPlural(numberOfBooks, texts.IdaLibraryTitleEmpty, texts.IdaLibraryTitleSingular, texts.IdaLibraryTitlePlural(numberOfBooks)))
      .iconFromChar('I')
      .addFluent(jToolBar, BorderLayout.NORTH)
      .addFluent(controller.component, BorderLayout.CENTER)
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