package eu.eyan.idakonyvtar

import eu.eyan.util.swing.WithComponent
import eu.eyan.util.swing.JTabbedPanePlus
import eu.eyan.idakonyvtar.controller.LibraryEditor
import java.awt.Frame
import javax.swing.JFrame
import eu.eyan.util.rx.lang.scala.subjects.BehaviorSubjectPlus.BehaviorSubjectImplicit
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.idakonyvtar.text.TextsIda
import eu.eyan.idakonyvtar.util.DialogHelper.YES
import eu.eyan.idakonyvtar.util.DialogHelper.NO
import java.awt.Component
import java.awt.event.ActionEvent
import eu.eyan.idakonyvtar.controller.LibraryEditor
import eu.eyan.idakonyvtar.controller.LibraryEditor
import rx.lang.scala.Observable
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import java.io.File
import eu.eyan.log.Log
import eu.eyan.util.text.Text
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.scala.Try
import eu.eyan.idakonyvtar.util.ExcelHandler
import eu.eyan.util.rx.lang.scala.ObservablePlus.ObservableImplicit

class IdaLibraryMultiEditor extends WithComponent {
  def getComponent = tabbedPane
  def confirmExit(frame: JFrame) = askToSaveIfDirty(frame, tabbedPane.items.toList)
  def deleteBookInActiveLibrary(evt: ActionEvent) = getActiveTab foreach deleteBook
  def createNewBookInActiveLibrary = getActiveTab foreach createNewBook
  def saveActiveLibraryAs(fileOnSuccess: File => Unit) = getActiveTab foreach chooseFileToSaveAsLibrary(fileOnSuccess)
  def saveActiveLibrary: Unit = getActiveTab foreach saveLibrary
  def chooseFileAndLoad: Unit = DialogHelper.fileChooser(null, lastLoadDirectoryRegistry.read.getOrElse(LOCAL_DIR).asFile, XLS, texts.LoadFileTexts, loadFileAndRememberDir)
  def loadLibrary(file: File) = loadLibraryFromFile(file)
  def getActiveEditor = activeEditor

  private val texts = new TextsIda
  private val lastLoadDirectoryRegistry = IdaLibrary.registryValue(LAST_LOAD_DIRECTORY)
  private val lastLoadedFiles = IdaLibrary.registryValue(LAST_LOADED_FILES)
  private val lastActiveFile = IdaLibrary.registryValue(LAST_ACTIVE_FILE)
  private val tabbedPane = new JTabbedPanePlus[LibraryEditor]()

  private val activeEditor = tabbedPane.activeTabObservable
  private val editors = tabbedPane.tabsObservable

  lastLoadedFiles.readMore.foreach(loadLibraries)
  editors.subscribe(saveLastLoadedFiles _)

  lastActiveFile.read.foreach(path => findControllerToFile(path.asFile).foreach(activateTab))
  activeEditor.subscribe(saveActiveEditor _)

  private def saveActiveEditor(editor: Option[LibraryEditor]) = lastActiveFile.save(editor.map(_.file.toString).getOrElse(""))

  private def saveLastLoadedFiles(editors: List[LibraryEditor]) = lastLoadedFiles.saveMore(editors.map(_.file.toString).toArray)

  private def getActiveTab = tabbedPane.getActiveTab

  private def askToSaveIfDirty(frame: JFrame, tabs: List[LibraryEditor]): Boolean = {
    if (tabs.nonEmpty) {
      if (tabs.head.isDirtyObservable.get) {
        val res = DialogHelper.yesNoCancel(null, texts.ExitSaveLibraryTexts(tabs.head.file.getName))
        if (res == YES) { tabs.head.saveLibrary; askToSaveIfDirty(frame, tabs.tail) }
        else if (res == NO) askToSaveIfDirty(frame, tabs.tail)
        else false
      } else askToSaveIfDirty(frame, tabs.tail)
    } else true
  }

  private def deleteBook(library: LibraryEditor) = library deleteBook

  private def createNewBook(library: LibraryEditor) = library createNewBook

  private def chooseFileToSaveAsLibrary(fileOnSuccess: File => Unit)(library: LibraryEditor) = DialogHelper.fileChooser(null, library.file, XLS, texts.SaveAsFileTexts, librarySaveAs(library, fileOnSuccess))
  private def librarySaveAs(library: LibraryEditor, fileOnSuccess: File => Unit)(selectedFile: File) = {
    val res = library.saveAsLibrary(selectedFile)
    if(res) fileOnSuccess(selectedFile)
  }

  private def saveLibrary(library: LibraryEditor) = library saveLibrary

  private def loadLibraries(fileList: Array[String]): Unit = fileList.map(_.asFile).filter(_.exists).foreach(loadLibraryFromFile)

  private def loadFileAndRememberDir(selectedFile: File) = {
    Log.info("selected file: " + selectedFile)
    lastLoadDirectoryRegistry.save(selectedFile.getParent)
    loadLibrary(selectedFile)
  }

  private def activateTab(controller: LibraryEditor) = tabbedPane.setSelectedComponent(controller.getComponent)
  private def findControllerToFile(file: File) = tabbedPane.items.find(_.file == file)
  private def loadLibraryFromFile(file: File) = {
    if (findControllerToFile(file).nonEmpty) activateTab(findControllerToFile(file).get)
    else {
      Log.info("Loading library: " + file)
      val library = Try(ExcelHandler.readLibrary(file))
      // if (columnNamesAndIndexesToShow.size < 1) throw new IllegalArgumentException("Legalább 1 oszlopot meg kell jeleníteni! Az oszlop konfigurációban helyesen kell konfigurálni.") // FIXME: not here...

      if (library.isSuccess) {
        val libraryController = new LibraryEditor(library.get)
        val dirtyText = libraryController.isDirtyObservable.map(dirtyToText)
        tabbedPane.addTab(libraryController, new Text(libraryController.file.withoutExtension.getName + PARAM, dirtyText), libraryController.file.toString, closeLibrary _)
      } else {
        //FIXME error handling
        //if (library.isFailure) bookTable.setEmptyText(ERROR_AT_READING_LIBRARY + ": " + library.failed.get.getMessage)
      }
    }
  }

  private def dirtyToText(dirty: Boolean) = if (dirty) DIRTY else NOT_DIRTY

  private def closeLibrary(libraryController: LibraryEditor): Unit = {
    if (libraryController.isDirtyObservable.get) {
      val res = DialogHelper.yesNoCancel(null, texts.CloseLibraryWindowTexts(libraryController.file.getName))
      if (res == YES) { libraryController.saveLibrary; tabbedPane.removeTab(libraryController) }
      else if (res == NO) tabbedPane.removeTab(libraryController)
      else { /* cancel -> do nothing */ }
    } else tabbedPane.removeTab(libraryController)
  }
}