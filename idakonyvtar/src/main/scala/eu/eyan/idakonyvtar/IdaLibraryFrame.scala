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
import eu.eyan.util.text.Text.emptySingularPlural
import javax.swing.JFrame
import javax.swing.JToolBar
import rx.lang.scala.Subscription
import rx.lang.scala.subjects.BehaviorSubject
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import scala.io.Codec

object IdaLibraryFrame {
  def apply(fileToOpen:  Option[File]) = new IdaLibraryFrame().startLibrary(fileToOpen)
}

class IdaLibraryFrame private () {

  def startLibrary(fileToOpen: Option[File]) = {
    checkAndselectLanguage

    val multiEditor = new IdaLibraryMultiEditor()
    fileToOpen.foreach(multiEditor.loadLibrary)

    multiEditor.getActiveEditor.combineLatest(filterText).subscribe(onFilterChanged _)

    multiEditor.getActiveEditor.subscribe(onNewActiveTab _)

    val jToolBar = new JToolBar(BASIC_FUNCTIONS)
    jToolBar.addButton(texts.ToolbarSaveButton).name(SAVE_LIBRARY).onAction(multiEditor.saveActiveLibrary).enabled(isBookOpen)
    jToolBar.addButton(texts.ToolbarLoadButton).name(LOAD_LIBRARY).onAction(multiEditor.chooseFileAndLoad)
    jToolBar.addButton(texts.ToolbarNewBookButton).name(ADD_NEW_BOOK).onAction(multiEditor.createNewBookInActiveLibrary).enabled(isBookOpen)
    jToolBar.addButton(texts.ToolbarDeleteBookButton).name(DELETE_BOOK).onActionPerformedEvent(multiEditor.deleteBookInActiveLibrary).enabled(isBookSelected)
    jToolBar.addLabel(texts.ToolbarFilterLabel)
    jToolBar.addTextField(5, EMPTY_STRING, FILTER).widthSet(200).onTextChanged(filterText).onHierarchyChangedEvent(_.getComponent.requestFocusInWindow)

    new JFrame()
      .name(classOf[IdaLibrary].getName)
      .title(emptySingularPlural(numberOfBooks, texts.IdaLibraryTitleEmpty, texts.IdaLibraryTitleSingular, texts.IdaLibraryTitlePlural(numberOfBooks)))
      .iconFromChar('I')
      .addFluent(jToolBar, BorderLayout.NORTH)
      .addFluent(multiEditor.getComponent, BorderLayout.CENTER)
      .menuItem(texts.MenuFile, texts.MenuFileLoad, multiEditor.chooseFileAndLoad)
      .menuItem(texts.MenuFile, texts.MenuFileSave, multiEditor.saveActiveLibrary, isBookOpen)
      .menuItem(texts.MenuFile, texts.MenuFileSaveAs, multiEditor.saveActiveLibraryAs(multiEditor.loadLibrary), isBookOpen)
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

  private val texts = new TextsIda

  private val numberOfBooks = BehaviorSubject(0)
  private var numberOfBooksSubscription: Option[Subscription] = None

  private val isBookSelected = BehaviorSubject(false)
  private var isBookSelectedSubscription: Option[Subscription] = None

  private val isBookOpen = BehaviorSubject(false)

  private val filterText = BehaviorSubject(EMPTY_STRING)

  private def selectLanguageByDialog(languages: Array[String]) = Alert.alertOptions(LANGUAGE_SELECTION, PLEASE_SELECT_YOUR_LANGUAGE, languages)

  private def getAllLogs = { DialogHelper.yes(texts.CopyLogsTexts); Log.toString }

  private def showAbout: Unit = DialogHelper.yes(texts.AboutWindowTexts)

  private def writeEmail = Desktop.getDesktop.mail(new URI(WRITE_EMAIL + URLEncoder.encode(getAllLogs, UTF8).replace(PLUS_AS_REGEX, SPACE_URL_ENCODED)))

  private def checkAndselectLanguage = if (texts.initialLanguage.isEmpty) selectLanguageByDialog(texts.languages).foreach(texts.onLanguageSelected)

  private def confirmEditorAndExit(multiEditor: IdaLibraryMultiEditor)(frame: JFrame) = multiEditor.confirmExit(frame) && DialogHelper.yesNo(frame, texts.ExitWindowTexts)

  private def setFilterInLibrary(text: String)(library: LibraryEditor) = library.setAllColumnFilter(text)

  private def onFilterChanged(tabAndFilter: (Option[LibraryEditor], String)) = tabAndFilter._1.foreach(setFilterInLibrary(tabAndFilter._2))

  private def onNewActiveTab(library: Option[LibraryEditor]): Unit = {
    Log.debug(library)
    numberOfBooksSubscription.foreach(_.unsubscribe)
    isBookSelectedSubscription.foreach(_.unsubscribe)
    if (library.isEmpty) {
      numberOfBooks.onNext(0)
      isBookSelected.onNext(false)
      isBookOpen.onNext(false)
    } else {
      numberOfBooksSubscription = Some(library.get.numberOfBooksObservable.subscribe(numberOfBooks))
      isBookSelectedSubscription = Some(library.get.isBookSelectedObservable.subscribe(isBookSelected))
      isBookOpen.onNext(true)
    }
  }

}