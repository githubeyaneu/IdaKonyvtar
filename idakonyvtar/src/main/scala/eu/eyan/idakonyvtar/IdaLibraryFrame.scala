package eu.eyan.idakonyvtar

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.io.File
import java.net.URI
import java.net.URLEncoder

import eu.eyan.idakonyvtar.controller.LibraryController
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
  def apply(fileToOpen: File) = new IdaLibraryFrame().startLibrary(fileToOpen)
}

class IdaLibraryFrame private () {

  private val texts = new TextsIda

  private def selectLanguageByDialog(languages: Array[String]) = Alert.alertOptions(LANGUAGE_SELECTION, PLEASE_SELECT_YOUR_LANGUAGE, languages)

  private def confirmExit(frame: JFrame) = askToSaveIfDirty(frame, tabs.items.toList)

  def askToSaveIfDirty(frame: JFrame, tabs: List[LibraryController]): Boolean = {
    if (tabs.nonEmpty) {
      if (tabs.head.isDirty.get) {
        val res = DialogHelper.yesNoCancel(null, texts.ExitSaveLibraryTexts(tabs.head.file.getName))
        if (res == YES) { tabs.head.saveLibrary; askToSaveIfDirty(frame, tabs.tail) }
        else if (res == NO) askToSaveIfDirty(frame, tabs.tail)
        else false
      } else askToSaveIfDirty(frame, tabs.tail)
    } else
      DialogHelper.yesNo(frame, texts.ExitWindowTexts)
  }

  private def getAllLogs = { DialogHelper.yes(texts.CopyLogsTexts); Log.toString }

  private def showAbout: Unit = DialogHelper.yes(texts.AboutWindowTexts)

  private def writeEmail = Desktop.getDesktop.mail(new URI(WRITE_EMAIL + URLEncoder.encode(getAllLogs, UTF8).replace(PLUS_AS_REGEX, SPACE_URL_ENCODED)))

  private val lastLoadDirectoryRegistry = IdaLibrary.registryValue(LAST_LOAD_DIRECTORY)

  private val lastLoadedFiles = IdaLibrary.registryValue(LAST_LOADED_FILES)

  private val tabs = new JTabbedPanePlus[LibraryController]()

  private val numberOfBooks = BehaviorSubject(0)
  private var numberOfBooksSubscription: Option[Subscription] = None

  private val isBookSelected = BehaviorSubject(false)
  private var isBookSelectedSubscription: Option[Subscription] = None

  private val isBookOpen = BehaviorSubject(false)

  val filterText = BehaviorSubject(EMPTY_STRING)

  def startLibrary(fileToOpen: File) = {
    if (texts.initialLanguage.isEmpty) selectLanguageByDialog(texts.languages).foreach(texts.onLanguageSelected)

    lastLoadedFiles.readMore.foreach(loadLibraries)
    loadLibraryFromFile(fileToOpen)
    def loadLibraries(fileList: Array[String]): Unit = fileList.map(_.asFile).filter(_.exists).foreach(loadLibraryFromFile)
    def loadLibraryFromFile(file: File): Unit = loadLibrary(new LibraryController(file))
    def loadLibrary(libraryController: LibraryController): Unit = {
      val alreadyOpen = tabs.items.find(_.file == libraryController.file)
      if (alreadyOpen.nonEmpty) tabs.setSelectedComponent(alreadyOpen.get.getComponent)
      else {
        def dirtyToText(dirty: Boolean) = if (dirty) DIRTY else NOT_DIRTY
        val dirtyText = libraryController.isDirty.map(dirtyToText)
        tabs.addTab(libraryController, new Text(libraryController.file.withoutExtension.getName + PARAM, dirtyText), libraryController.file.toString, closeLibrary _)
        val files = tabs.items.map(_.file.toString)
        Log.info(files)
        lastLoadedFiles.saveMore(files.toArray)
      }
    }

    def closeLibrary(libraryController: LibraryController): Unit = {
      if (libraryController.isDirty.get) {
        val res = DialogHelper.yesNoCancel(null, texts.CloseLibraryWindowTexts(libraryController.file.getName))
        if (res == YES) { libraryController.saveLibrary; tabs.removeTab(libraryController) }
        else if (res == NO) tabs.removeTab(libraryController)
      } else tabs.removeTab(libraryController)
    }

    def loadFile(selectedFile: File) = {
      Log.info("selected file: " + selectedFile)
      lastLoadDirectoryRegistry.save(selectedFile.getParent)
      loadLibrary(new LibraryController(selectedFile))
    }

    def chooseFileToLoad: Unit = DialogHelper.fileChooser(null, lastLoadDirectoryRegistry.read.getOrElse(LOCAL_DIR).asFile, XLS, texts.LoadFileTexts, loadFile)

    def saveLibrary: Unit = tabs.getActiveTab.foreach(_.saveLibrary)

    def librarySaveAs(library: LibraryController)(selectedFile: File) = {
      Log.info("selected file: " + selectedFile)
      library.saveAsLibrary(selectedFile)
      loadLibraryFromFile(selectedFile)
    }
    def chooseFileToSaveAsLibrary(library: LibraryController) = DialogHelper.fileChooser(null, library.file, XLS, texts.SaveAsFileTexts, librarySaveAs(library))
    def saveAsActiveLibrary = tabs.getActiveTab.foreach(chooseFileToSaveAsLibrary)

    def createNewBook(library: LibraryController) = library.createNewBook
    def createNewBookInActiveLibrary: Unit = tabs.getActiveTab.foreach(createNewBook)

    def setFilterInLibrary(text: String)(library: LibraryController) = library.filter(text)
    def setFilter(tabAndFilter: (Option[LibraryController], String)) = tabAndFilter._1.foreach(setFilterInLibrary(tabAndFilter._2))
    tabs.activeTab.combineLatest(filterText).subscribe(setFilter _)

    def deleteBookOnActiveTab(evt: ActionEvent) = tabs.getActiveTab.foreach(_.deleteBook(evt.getSource.asInstanceOf[Component]))

    tabs.activeTab.subscribe(onNewActiveTab _)
    def onNewActiveTab(controller: Option[LibraryController]): Unit = {
      Log.debug(controller)
      numberOfBooksSubscription.foreach(_.unsubscribe)
      isBookSelectedSubscription.foreach(_.unsubscribe)
      if (controller.isEmpty) {
        numberOfBooks.onNext(0)
        isBookSelected.onNext(false)
        isBookOpen.onNext(false)
      } else {
        numberOfBooksSubscription = Some(controller.get.numberOfBooks.subscribe(numberOfBooks))
        isBookSelectedSubscription = Some(controller.get.isBookSelected.subscribe(isBookSelected))
        isBookOpen.onNext(true)
      }
    }

    val jToolBar = new JToolBar(BASIC_FUNCTIONS)
    jToolBar.addButton(texts.ToolbarSaveButton).name(SAVE_LIBRARY).onAction(saveLibrary).enabled(isBookOpen)
    jToolBar.addButton(texts.ToolbarLoadButton).name(LOAD_LIBRARY).onAction(chooseFileToLoad)
    jToolBar.addButton(texts.ToolbarNewBookButton).name(ADD_NEW_BOOK).onAction(createNewBookInActiveLibrary).enabled(isBookOpen)
    jToolBar.addButton(texts.ToolbarDeleteBookButton).name(DELETE_BOOK).onActionPerformedEvent(deleteBookOnActiveTab).enabled(isBookSelected)
    jToolBar.addLabel(texts.ToolbarFilterLabel)
    jToolBar.addTextField(5, EMPTY_STRING, FILTER).widthSet(200).onTextChanged(filterText).onHierarchyChangedEvent(_.getComponent.requestFocusInWindow)

    new JFrame()
      .name(classOf[IdaLibrary].getName)
      .title(emptySingularPlural(numberOfBooks, texts.IdaLibraryTitleEmpty, texts.IdaLibraryTitleSingular, texts.IdaLibraryTitlePlural(numberOfBooks)))
      .iconFromChar('I')
      .addFluent(jToolBar, BorderLayout.NORTH)
      .addFluent(tabs, BorderLayout.CENTER)
      .menuItem(texts.MenuFile, texts.MenuFileLoad, chooseFileToLoad)
      .menuItem(texts.MenuFile, texts.MenuFileSave, saveLibrary, isBookOpen)
      .menuItem(texts.MenuFile, texts.MenuFileSaveAs, saveAsActiveLibrary, isBookOpen)
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

  }
}