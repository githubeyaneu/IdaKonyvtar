package eu.eyan.idakonyvtar.controller

import java.awt.Component
import java.io.File

import scala.collection.JavaConversions.asScalaBuffer

import com.jgoodies.binding.list.SelectionInList

import eu.eyan.idakonyvtar.model.Book
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
import eu.eyan.idakonyvtar.model.Library
import eu.eyan.util.swing.TableCol
import eu.eyan.util.swing.TableRow
import rx.lang.scala.Observable
import eu.eyan.util.rx.lang.scala.ObservablePlus
import eu.eyan.util.text.Texts
import eu.eyan.util.text.Text
import eu.eyan.idakonyvtar.IdaLibrary
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import eu.eyan.util.rx.lang.scala.ObservablePlus.ObservableImplicitBoolean
import eu.eyan.util.scala.TryCatch
import eu.eyan.util.scala.TryCatchThrowable
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage

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
  private val previousBook = library.createEmptyBook

  private val numberOfBooks = BehaviorSubject(books.getList.size)
  private val hasBooks = numberOfBooks.distinctUntilChanged.map(_ > 0)
  private val tableEmptyText = hasBooks.ifElse(texts.NoResultAfterFilter, texts.EmptyLibrary)

  private val bookTable = new BookTable(file.getPath, library.columnNamesToShow, books, cellValue, tableEmptyText)

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
  books.setList(library.booksAsJavaList)

  private def cellValue(row: TableRow, col: TableCol) = books.getElementAt(row.index).getValue(library.columnFieldsToShow(col.index))

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
    TryCatch({ ExcelHandler.saveLibrary(file, library); notDirty }, (e: Throwable) => { Log.error(e); DialogHelper.yes(texts.SaveErrorTexts) })
  }

  private def checkAndSaveAsLibrary(newFile: File) = {
    Log.info("SaveAs " + file)
    def confirmOverwrite = DialogHelper.yesNo(null, texts.SaveAsOverwriteConfirmText(newFile), texts.SaveAsOverwriteConfirmWindowTitle, texts.SaveAsOverwriteYes, texts.SaveAsOverwriteNo)
    TryCatchThrowable(
      ((newFile.notExists || confirmOverwrite) && ExcelHandler.saveLibrary(newFile, library)),
      e => { Log.error(e); DialogHelper.yes(texts.SaveErrorTexts); false })
  }

  private def createNewBookInDialog = {
    val book = newPreviousBook

    val bookController = new BookController(book, library.getColumns, books.getList.toList, WITH_ISBN, file)

    val output = DialogHelper.yesNoEditor(component, bookController.getComponent, texts.NewBookWindowTitle, texts.NewBookSaveButton, texts.NewBookCancelButton)

    if (output) {
      saveImages(book)
      books.getList.add(0, book)
      savePreviousBook(book)
      books.fireIntervalAdded(0, 0)
      dirty
    }
  }

  private def editBook = {
    val selectedBookIndex = bookTable.getSelectedIndex

    val book = Book.copy(books.getList.get(selectedBookIndex))

    //TODO spagetti and refaactor the columns...
    loadImages(book)

    val bookController = new BookController(book, library.getColumns, books.getList.toList, NO_ISBN, file)

    val titleFieldName = texts.ConfigTitleFieldName
    val titleField = titleFieldName.map(library.fieldToName)
    val bookTitle = titleField.map(_.map(field => book.getValue(field)).getOrElse(EMPTY_STRING))

    val output = DialogHelper.yesNoEditor(component, bookController.getComponent, texts.EditBookWindowTitle(bookTitle), texts.EditBookSaveButton, texts.EditBookCancelButton)

    if (output) {
      saveImages(book)
      books.getList.set(selectedBookIndex, book)
      books.fireSelectedContentsChanged
      dirty
    }
  }

  private def loadImages(book: Book) = {
    for {
      //TODO spagetti: library.getPictureColumns
      field <- library.getColumns
      if field.isPicture
      if (book.getValue(field) != EMPTY_STRING)
    } book.setImage(field)(loadImage(book.getValue(field)))
  }
  
  private def saveImages(book: Book) = for {
    //TODO spagetti: library.getPictureColumns
    field <- library.getColumns
    if field.isPicture
    if (book.getValue(field) == EMPTY_STRING)
  } book.getImage(field).map(saveImageIntoNewFile).foreach(book.setValue(field))

  private def loadImage(imgName: String) = {
    val dir = file.getParentFile
    val imagesDir = (file.getAbsolutePath + ".images").asDir
    val imageFile = (imagesDir.getAbsolutePath + "\\" + imgName).asFile
    val image = ImageIO.read(imageFile)
    image
  }

  private def saveImageIntoNewFile(image: RenderedImage) = {
    val imagesDir = (file.getAbsolutePath + IMAGES_DIR_POSTFIX).asDir.mkDirs
    val imageFile = (imagesDir.getAbsolutePath + DEFAULT_IMAGE_NAME).asFile.generateNewNameIfExists()
    ImageIO.write(image, IMAGE_EXTENSION, imageFile)
    imageFile.getName
  }

  private def savePreviousBook(book: Book) = library.fieldsToRemember.foreach(field => previousBook.setValue(field)(book.getValue(field)))

  private def newPreviousBook: Book = {
    val newBook = library.createEmptyBook
    library.fieldsToRemember.foreach(field => {
      newBook.setValue(field)(previousBook.getValue(field))
      Log.info("Remembering col " + field + " val:" + previousBook.getValue(field))
    })
    newBook
  }
}