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
import rx.lang.scala.Observable
import eu.eyan.util.rx.lang.scala.ObservablePlus
import eu.eyan.util.text.Texts
import eu.eyan.util.text.Text
import eu.eyan.idakonyvtar.IdaLibrary
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import eu.eyan.util.rx.lang.scala.ObservablePlus.ObservableImplicitBoolean

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

  private val texts = IdaLibrary.texts
  private val books = new SelectionInList[Book]()
  private val previousBook = Book(library.columns.size)

  private val columnNamesAndIndexesToShow = library.columns.zipWithIndex.filter(x => columnToShowFilter(x._2))
  private val columnNames = columnNamesAndIndexesToShow.unzip._1
  private val columnIndicesToShow = columnNamesAndIndexesToShow.unzip._2 // TODO move to Library...???

  private val numberOfBooks = BehaviorSubject(books.getList.size)
  private val hasBooks = numberOfBooks.distinctUntilChanged.map(_ > 0)
  private val tableEmptyText = hasBooks.ifElse(texts.NoResultAfterFilter, texts.EmptyLibrary)

  private val bookTable = new BookTable(file.getPath, columnNames, books, cellValue, tableEmptyText)

  private val isBookSelected = BehaviorSubject(false)
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

  books.getList.clear
  books.setList(library.books)

  private def columnToShowFilter(col: Col) = library.configuration.isTrue(library.columns(col.index), ColumnConfigurations.SHOW_IN_TABLE)

  private def cellValue(row: Row, col: Col) = books.getElementAt(row.index).getValue(columnIndicesToShow(col.index))
  private def columnToShowFilter(columnIndex: Int) = library.configuration.isTrue(library.columns(columnIndex), ColumnConfigurations.SHOW_IN_TABLE)

  private def dirty = isDirty.onNext(true)
  private def notDirty = isDirty.onNext(false)

  private def deleteBookWithDialog = {
    if (DialogHelper.yesNo(getComponent, texts.DeleteBookWindowTexts)) {
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
    Try { if (newFile.notExists || confirmOverwrite) { ExcelHandler.saveLibrary(newFile, library); success = true } }
    success
  }

  private def createNewBookInDialog = {
    val book = newPreviousBook(library.columns.size)
    val columns = library.columns.toList
    val columnConfiguration = library.configuration
    val bookList = books.getList.toList
    val isbnEnabled = true
    val loadedFile = file

    val bookController = new BookController(book, columns, columnConfiguration, bookList, isbnEnabled, loadedFile)

    val editorDialog = DialogHelper.startModalDialog(component, bookController)

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

    val book = Book(books.getList.get(selectedBookIndex))
    val columns = library.columns.toList
    val columnConfiguration = library.configuration
    val bookList = books.getList.toList
    val isbnEnabled = false
    val loadedFile = file

    val bookController = new BookController(book, columns, columnConfiguration, bookList, isbnEnabled, loadedFile)

    val editorDialog = DialogHelper.startModalDialog(component, bookController)

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
      if (imageName == EMPTY_STRING && book.images.contains(i)) {
        val dir = file.getParentFile
        val imagesDir = (file.getAbsolutePath + IMAGES_DIR_POSTFIX).asDir
        if (!imagesDir.exists) imagesDir.mkdir
        val imageFile = (imagesDir.getAbsolutePath + DEFAULT_IMAGE_NAME).asFile.generateNewNameIfExists()
        ImageIO.write(book.images.get(i).get, IMAGE_EXTENSION, imageFile)
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