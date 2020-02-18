package eu.eyan.idakonyvtar.controller

import java.awt.image.RenderedImage
import java.io.File

import com.jgoodies.binding.list.SelectionInList
import eu.eyan.idakonyvtar.IdaLibrary
import eu.eyan.idakonyvtar.model.{Book, BookField, Library}
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import eu.eyan.idakonyvtar.util.{DialogHelper, LibraryExcelHandler}
import eu.eyan.idakonyvtar.view.BookTable
import eu.eyan.log.Log
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.jgoodies.SelectionInListPlus.SelectionInListImplicit
import eu.eyan.util.rx.lang.scala.ObservablePlus.ObservableImplicitBoolean
import eu.eyan.util.rx.lang.scala.subjects.BehaviorSubjectPlus.BehaviorSubjectImplicit
import eu.eyan.util.scala.{TryCatch, TryCatchThrowable}
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.swing.{JPanelWithFrameLayout, TableCol, TableRow, WithComponent}
import javax.imageio.ImageIO
import rx.lang.scala.subjects.BehaviorSubject

import collection.JavaConverters._

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
  private var previousBook = library.createEmptyBook

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
    TryCatch({ LibraryExcelHandler.saveLibrary(file, library); notDirty }, (e: Throwable) => { Log.error(e); DialogHelper.yes(texts.SaveErrorTexts) })
  }

  private def checkAndSaveAsLibrary(newFile: File) = {
    Log.info("SaveAs " + file)
    def confirmOverwrite = DialogHelper.yesNo(null, texts.SaveAsOverwriteConfirmText(newFile), texts.SaveAsOverwriteConfirmWindowTitle, texts.SaveAsOverwriteYes, texts.SaveAsOverwriteNo)
    TryCatchThrowable(
      (newFile.notExists || confirmOverwrite) && LibraryExcelHandler.saveLibrary(newFile, library),
      e => { Log.error(e); DialogHelper.yes(texts.SaveErrorTexts); false })
  }

  private def createNewBookInDialog = {
    val bookController = BookEditor.editWithIsbn(previousBook, library.getColumns, books.getList.asScala.toList, file)

    val output = DialogHelper.yesNoEditor(component, bookController.getComponent, texts.NewBookWindowTitle, texts.NewBookSaveButton, texts.NewBookCancelButton)

    if (output) {
      val newBook = bookController.getResult
      saveImages(newBook)
      books.getList.add(0, newBook)
      savePreviousBook(newBook)
      books.fireIntervalAdded(0, 0)
      dirty
    }
  }

  private def editBook = {
    val selectedBookIndex = bookTable.getSelectedIndex

    val originalBook = books.getList.get(selectedBookIndex)

    //TODO spagetti and refaactor the columns...
    val pictures = loadPictures(originalBook)
    val originalBookWithPictures = originalBook.withPictures(pictures)

    val bookController = BookEditor.editBookWithoutIsbn(originalBookWithPictures, library.getColumns, books.getList.asScala.toList, file)

    val titleFieldName = texts.ConfigTitleFieldName
    val titleField = titleFieldName.map(library.fieldToName)
    val bookTitle = titleField.map(_.map(field => originalBookWithPictures.getValue(field)).getOrElse(EMPTY_STRING))

    val output = DialogHelper.yesNoEditor(component, bookController.getComponent, texts.EditBookWindowTitle(bookTitle), texts.EditBookSaveButton, texts.EditBookCancelButton)

    if (output) {
      val editedBook = bookController.getResult
      saveImages(editedBook)
      books.getList.set(selectedBookIndex, editedBook)
      books.fireSelectedContentsChanged
      dirty
    }
  }

  private def loadPictures(book: Book) = {
	  def isFieldPictureToLoad(pair: (BookField, String)) = pair match { case (field, value) => field.isPicture && value != EMPTY_STRING }
	  book.getValues.filter(isFieldPictureToLoad).mapValues(loadImage)
  }

  private def saveImages(book: Book): Book = {
	  def saveImage(pair: (BookField, String)) = pair match { case (field, _) => (field, book.getImage(field).map(saveImageIntoNewFile).get) }
	  def isFieldPictureToSave(pair: (BookField, String)) = pair match { case (field, value) => field.isPicture && value == EMPTY_STRING && book.getImage(field).nonEmpty}
    val imageFieldsToSave = book.getValues filter isFieldPictureToSave
    val newImageNames = imageFieldsToSave map saveImage
    val updatedFields = book.getValues ++ newImageNames
    Book(updatedFields.toList)
  }

  private def loadImage(imgName: String) = {
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

  private def savePreviousBook(book: Book) = {
    def isFieldToRemember(pair: (BookField, String)) = pair match { case (field, _) => field.isRemember}
    val fieldsToRemember = book.getValues filter isFieldToRemember
    val updatedFields = previousBook.getValues ++ fieldsToRemember
    previousBook = Book(updatedFields.toList)
  }
}