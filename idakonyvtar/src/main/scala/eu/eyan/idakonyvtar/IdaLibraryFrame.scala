package eu.eyan.idakonyvtar

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.io.File
import java.net.URI
import java.net.URLEncoder

import eu.eyan.idakonyvtar.controller.LibraryEditor
import eu.eyan.idakonyvtar.text.TextsIda
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.idakonyvtar.util.DialogHelper.NO
import eu.eyan.idakonyvtar.util.DialogHelper.YES
import eu.eyan.idakonyvtar.util.WebCam
import eu.eyan.log.Log
import eu.eyan.log.LogWindow
import eu.eyan.util.awt.clipboard.ClipboardPlus
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.registry.RegistryPlus
import eu.eyan.util.rx.lang.scala.subjects.BehaviorSubjectPlus.BehaviorSubjectImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.swing.Alert
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.JFramePlus
import eu.eyan.util.swing.JFramePlus.JFramePlusImplicit
import eu.eyan.util.swing.JTabbedPanePlus
import eu.eyan.util.swing.JTextFieldPlus.JTextFieldPlusImplicit
import eu.eyan.util.swing.JToolBarPlus.JToolBarImplicit
import eu.eyan.util.text.Text
import javax.swing.JFrame
import javax.swing.JToolBar
import rx.lang.scala.Subscription
import rx.lang.scala.subjects.BehaviorSubject
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import scala.io.Codec
import eu.eyan.util.rx.lang.scala.ObservablePlus.ObservableImplicitInt
import eu.eyan.util.rx.lang.scala.ObservablePlus
import rx.lang.scala.Observable

object IdaLibraryFrame {
  def apply(fileToOpen: Option[File]) = new IdaLibraryFrame().startLibrary(fileToOpen)
}

class IdaLibraryFrame private () {

  def startLibrary(fileToOpen: Option[File]) = {
    checkAndselectLanguage

    fileToOpen.foreach(multiEditor.loadLibrary)

    multiEditor.getActiveEditor.combineLatest(filterText).subscribe(onFilterChanged _)

    val jToolBar = new JToolBar(BASIC_FUNCTIONS)
    jToolBar.addButton(texts.ToolbarSaveButton).name(SAVE_LIBRARY).onAction(multiEditor.saveActiveLibrary).enabled(isAnyLibraryOpen)
    jToolBar.addButton(texts.ToolbarLoadButton).name(LOAD_LIBRARY).onAction(multiEditor.chooseFileAndLoad)
    jToolBar.addButton(texts.ToolbarNewBookButton).name(ADD_NEW_BOOK).onAction(multiEditor.createNewBookInActiveLibrary).enabled(isAnyLibraryOpen)
    jToolBar.addButton(texts.ToolbarDeleteBookButton).name(DELETE_BOOK).onActionPerformedEvent(multiEditor.deleteBookInActiveLibrary).enabled(isBookSelected)
    jToolBar.addLabel(texts.ToolbarFilterLabel)
    jToolBar.addTextField(5, EMPTY_STRING, FILTER).widthSet(200).onTextChanged(filterText).onHierarchyChangedEvent(_.getComponent.requestFocusInWindow)

    new JFrame()
      .name(classOf[IdaLibrary].getName)
      .title(numberOfBooks.emptySingularPlural(texts.IdaLibraryTitleEmpty, texts.IdaLibraryTitleSingular, texts.IdaLibraryTitlePlural(numberOfBooks)))
      .iconFromChar('I')
      .addFluent(jToolBar, BorderLayout.NORTH)
      .addFluent(multiEditor.getComponent, BorderLayout.CENTER)
      .menuItem(texts.MenuFile, texts.MenuFileLoad, multiEditor.chooseFileAndLoad)
      .menuItem(texts.MenuFile, texts.MenuFileSave, multiEditor.saveActiveLibrary, isAnyLibraryOpen)
      .menuItem(texts.MenuFile, texts.MenuFileSaveAs, multiEditor.saveActiveLibraryAs(multiEditor.loadLibrary), isAnyLibraryOpen)
      .menuItemSeparator(texts.MenuFile)
      .menuItemEvent(texts.MenuFile, texts.MenuFileExit, JFramePlus.close)
      .menuItems(texts.MenuLanguages, texts.languages, texts.onLanguageSelected: String => Unit)
      .menuItemEvent(texts.MenuDebug, texts.MenuDebugLogWindow, LogWindow.show)
      .menuItem(texts.MenuDebug, texts.MenuDebugCopyLogs, ClipboardPlus.copyToClipboard(getAllLogs))
      .menuItem(texts.MenuDebug, texts.MenuDebugClearRegistry, RegistryPlus.clear(classOf[IdaLibrary].getName))
      .menuItem(texts.MenuHelp, texts.MenuHelpEmailError, writeEmail)
      .menuItem(texts.MenuHelp, texts.MenuHelpAbout, showAbout)
      .onCloseDisposeWithCondition(confirmEditorAndExit(multiEditor))
      .onWindowClosed({ LogWindow.close; WebCam.stop })
      .packAndSetVisible
      .positionToCenter
      .maximize
  }

  private val texts = IdaLibrary.texts

  private val filterText = BehaviorSubject(EMPTY_STRING)

  private val multiEditor = new IdaLibraryMultiEditor()

  private val numberOfBooks = multiEditor.getActiveEditor.map(getLibraryNrOfBooksOr0).switch

  private val isBookSelected = multiEditor.getActiveEditor.map(getLibraryIsBookSelectedOrNo).switch

  private val isAnyLibraryOpen = multiEditor.getActiveEditor.map(libraryNonEmpty)

  private def getLibraryIsBookSelectedOrNo(editorOpt: Option[LibraryEditor]) = editorOpt.map(_.isBookSelectedObservable).getOrElse(BehaviorSubject(false))

  private def getLibraryNrOfBooksOr0(editorOpt: Option[LibraryEditor]) = editorOpt.map(_.numberOfBooksObservable).getOrElse(BehaviorSubject(0))

  private def selectLanguageByDialog(languages: Array[String]) = Alert.alertOptions(LANGUAGE_SELECTION, PLEASE_SELECT_YOUR_LANGUAGE, languages)

  private def getAllLogs = { DialogHelper.yes(texts.CopyLogsTexts); Log.toString }

  private def showAbout: Unit = DialogHelper.yes(texts.AboutWindowTexts)

  private def writeEmail = Desktop.getDesktop.mail(new URI(WRITE_EMAIL + URLEncoder.encode(getAllLogs, UTF8).replace(PLUS_AS_REGEX, SPACE_URL_ENCODED)))

  private def checkAndselectLanguage = if (texts.initialLanguage.isEmpty) selectLanguageByDialog(texts.languages).foreach(texts.onLanguageSelected)

  private def confirmEditorAndExit(multiEditor: IdaLibraryMultiEditor)(frame: JFrame) = multiEditor.confirmExit(frame) && DialogHelper.yesNo(frame, texts.ExitWindowTexts)

  private def setFilterInLibrary(text: String)(library: LibraryEditor) = library.setAllColumnFilter(text)

  private def onFilterChanged(tabAndFilter: (Option[LibraryEditor], String)) = tabAndFilter._1.foreach(setFilterInLibrary(tabAndFilter._2))

  private def libraryNonEmpty(libOpt: Option[LibraryEditor]) = libOpt.nonEmpty
}