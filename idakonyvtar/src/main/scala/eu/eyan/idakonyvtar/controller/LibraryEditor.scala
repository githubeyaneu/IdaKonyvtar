package eu.eyan.idakonyvtar.controller

import java.awt.Component
import java.io.File

import scala.collection.JavaConversions.asScalaBuffer

import com.jgoodies.binding.list.SelectionInList

import eu.eyan.idakonyvtar.controller.input.BookControllerInput
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.ColumnConfigurations
import eu.eyan.idakonyvtar.text.TechnicalTextsIda.ERROR_AT_READING_LIBRARY
import eu.eyan.idakonyvtar.text.TextsIda
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.idakonyvtar.util.ExcelHandler
import eu.eyan.idakonyvtar.util.LibraryException
import eu.eyan.idakonyvtar.view.BookTable
import eu.eyan.log.Log
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.jgoodies.SelectionInListPlus.SelectionInListImplicit
import eu.eyan.util.rx.lang.scala.subjects.BehaviorSubjectPlus.BehaviorSubjectImplicit
import eu.eyan.util.scala.Try
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.swing.JPanelWithFrameLayout
import eu.eyan.util.swing.WithComponent
import javax.imageio.ImageIO
import javax.swing.JOptionPane
import rx.lang.scala.subjects.BehaviorSubject
import com.jgoodies.binding.adapter.AbstractTableAdapter
import javax.swing.ListModel
import eu.eyan.idakonyvtar.model.ColumnKonfiguration
import eu.eyan.idakonyvtar.model.Library
import eu.eyan.util.swing.Col
import eu.eyan.util.swing.Row

class LibraryEditor(val library: Library) extends WithComponent {
  override def toString = s"LibraryController[file=${library.file}, nrOfBooks=${numberOfBooks.get}, isBookSelected=${isBookSelected.get}]"
  def getComponent = component
  
  def createNewBook = createNewBookInDialog
  def saveAsLibrary(newFile: File) = checkAndSaveAsLibrary(newFile)
  def saveLibrary = checkAndSaveLibrary
  def deleteBook = deleteBookWithDialog
  def setAllColumnFilter(textToFilter: String) = bookTable setAllColumnFilter textToFilter
  def file = library.file
  def numberOfBooksObservable = numberOfBooks.distinctUntilChanged
  def isBookSelectedObservable = isBookSelected.distinctUntilChanged
  def isDirtyObservable = isDirty.distinctUntilChanged

  private val books = new SelectionInList[Book]()
  private val previousBook = Book(library.columns.size)

  private val columnNamesAndIndexesToShow = library.columns.zipWithIndex.filter(x => columnToShowFilter(x._2))
  private val columnNames = columnNamesAndIndexesToShow.unzip._1
  private val columnIndicesToShow = columnNamesAndIndexesToShow.unzip._2 // TODO move to Library...???

  private val bookTable = new BookTable(file.getPath, columnNames, books, cellValue)

  private val texts = new TextsIda
  private val isBookSelected = BehaviorSubject(false)
  private val numberOfBooks = BehaviorSubject(books.getList.size)
  private val isDirty = BehaviorSubject(false)

  private val component = new JPanelWithFrameLayout()
    .withBorders
    .withSeparators
    .newColumnFPG
    .newRowFPG
    .add(bookTable.getComponent)

  books.onListData(numberOfBooks.onNext(books.getList.size))
  
  bookTable.onLineDoubleClicked(editBook)
  bookTable.onSelectionChanged(selectedRow => isBookSelected.onNext(selectedRow >= 0))

  numberOfBooks.distinctUntilChanged.subscribe(refreshErrorMessage _)
  books.getList.clear
  books.setList(library.books)

  private def columnToShowFilter(col: Col) = library.configuration.isTrue(library.columns(col.index), ColumnConfigurations.SHOW_IN_TABLE)

  private def cellValue(row: Row, col: Col) = books.getElementAt(row.index).getValue(columnIndicesToShow(col.index)) 
  private def columnToShowFilter(columnIndex: Int) = library.configuration.isTrue(library.columns(columnIndex), ColumnConfigurations.SHOW_IN_TABLE)

  private def dirty = isDirty.onNext(true)
  private def notDirty = isDirty.onNext(false)

  private def refreshErrorMessage(nrOfBooks: Int) = { //TODO rename
    Log.info(nrOfBooks + "")
    if (nrOfBooks > 0) bookTable.setEmptyText("Ilyen szűrőfeltételekkel nem található book.")
    else bookTable.setEmptyText("Nincs book a listában.")
  }

  private def deleteBookWithDialog = {
    if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog( // TODO DialogHelper.yesNo
      getComponent,
      "Biztosan törölni akarod?",
      "Törlés megerősítése",
      JOptionPane.YES_NO_OPTION,
      JOptionPane.QUESTION_MESSAGE,
      null,
      Array("Igen", "Nem"),
      "Nem")) {

      val selectionIndex = books.getSelectionIndex
      books.getList.remove(selectionIndex)
      books.fireIntervalRemoved(selectionIndex, selectionIndex)
      dirty
    }
  }

  private def checkAndSaveLibrary = {
    Log.info("Save " + file)
    Try { ExcelHandler.saveLibrary(file, library); notDirty } // TODO: error alert if failed.
  }

  private def checkAndSaveAsLibrary(newFile: File) = {
    Log.info("SaveAs " + file)
    def confirmOverwrite = DialogHelper.yesNo(null, texts.SaveAsOverwriteConfirmText(newFile), texts.SaveAsOverwriteConfirmWindowTitle, texts.SaveAsOverwriteYes, texts.SaveAsOverwriteNo)
    var success = false
    Try { if (newFile.notExists || confirmOverwrite) {ExcelHandler.saveLibrary(newFile, library); success = true} }
    success
  }

  private def createNewBookInDialog = {
    val bookControllerInput = new BookControllerInput(newPreviousBook(library.columns.size), library.columns.toList, library.configuration, books.getList.toList, true, file)
    val bookController = new BookController

    val editorDialog = DialogHelper.startModalDialog(component, bookController, bookControllerInput)

    if (editorDialog.isOk) {
      saveImages(bookController.getOutput)
      books.getList.add(0, bookController.getOutput)
      savePreviousBook(bookController.getOutput)
      books.fireIntervalAdded(0, 0)
      dirty
    }
  }

  private def editBook = {
    val selectedBookIndex = bookTable.getSelectedIndex

    val bookControllerInput = new BookControllerInput(Book(books.getList.get(selectedBookIndex)), library.columns.toList, library.configuration, library.books.toList, false, file)
    val bookController = new BookController

    val editorDialog = DialogHelper.startModalDialog(component, bookController, bookControllerInput)

    if (editorDialog.isOk) {
      saveImages(bookController.getOutput)
      books.getList.set(selectedBookIndex, bookController.getOutput)
      books.fireSelectedContentsChanged
      dirty
    }
  }

  private def saveImages(book: Book) = {
    //TODO WTF is this spagetti? put all relevant shot to book and only tha save belongs here.
    val columns = library.columns.toList
    val columnConfiguration = library.configuration
    for { i <- 0 until columns.size } {
      val imageName = book.getValue(i)
      val columnName = columns(i)
      val isPictureField = columnConfiguration.isTrue(columnName, ColumnConfigurations.PICTURE)
      if (imageName == "" && book.images.contains(i)) {
        val dir = file.getParentFile
        val imagesDir = (file.getAbsolutePath + ".images").asDir
        if (!imagesDir.exists) imagesDir.mkdir
        val imageFile = (imagesDir.getAbsolutePath + "\\IMG.JPG").asFile.generateNewNameIfExists()
        ImageIO.write(book.images.get(i).get, "JPG", imageFile)
        book.values(i) = imageFile.getName
      } else {
        // do nothing image already there
      }
    }
  }

  private def savePreviousBook(book: Book) = {
    Log.info
    library.configuration.getRememberingColumns.foreach(colName => {
      val columnIndex = library.columns.indexOf(colName)
      previousBook.setValue(columnIndex, book.getValue(columnIndex))
    })
  }

  private def newPreviousBook(size: Int): Book = {
    val newBook = Book(size)
    library.configuration.getRememberingColumns.foreach(rememberingColumn => {
      val columnIndex = library.columns.indexOf(rememberingColumn)
      newBook.setValue(columnIndex, previousBook.getValue(columnIndex))
      Log.info("Remembering col " + columnIndex + " val:" + previousBook.getValue(columnIndex))
    })
    newBook
  }
}