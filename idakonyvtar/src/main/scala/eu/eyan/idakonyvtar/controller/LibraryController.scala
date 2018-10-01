package eu.eyan.idakonyvtar.controller

import java.awt.Component
import java.io.File

import scala.collection.JavaConversions.asScalaBuffer

import com.jgoodies.binding.adapter.SingleListSelectionAdapter
import com.jgoodies.binding.list.SelectionInList

import eu.eyan.idakonyvtar.controller.adapter.LibraryListTableModel
import eu.eyan.idakonyvtar.controller.input.BookControllerInput
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.ColumnConfigurations
import eu.eyan.idakonyvtar.model.Library
import eu.eyan.idakonyvtar.text.TextsIda
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.idakonyvtar.util.ExcelHandler
import eu.eyan.idakonyvtar.util.LibraryException
import eu.eyan.idakonyvtar.view.BookTable
import eu.eyan.log.Log
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.jgoodies.SelectionInListPlus.SelectionInListImplicit
import eu.eyan.util.rx.lang.scala.subjects.BehaviorSubjectPlus.BehaviorSubjectImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.swing.HighlightRenderer
import eu.eyan.util.swing.JPanelWithFrameLayout
import eu.eyan.util.swing.JTablePlus.JTableImplicit
import eu.eyan.util.swing.SpecialCharacterRowFilter
import eu.eyan.util.swing.SwingPlus.showErrorDialog
import eu.eyan.util.swing.WithComponent
import javax.imageio.ImageIO
import javax.swing.JOptionPane
import javax.swing.ListSelectionModel
import rx.lang.scala.subjects.BehaviorSubject
import eu.eyan.util.scala.Try
import eu.eyan.util.scala.TryCatch
import eu.eyan.util.scala.TryCatchThrowable
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import javax.swing.JScrollPane

class LibraryController(val file: File) extends WithComponent {
  override def toString = s"LibraryController[file=$file, nrOfBooks=${numberOfBooks.get}, isBookSelected=${isBookSelected.get}]"
  def getComponent = component
  private val bookTable = new BookTable(file.getPath)
  private val highlightRenderer = new HighlightRenderer
  private val books: SelectionInList[Book] = new SelectionInList[Book]()
  private val texts = new TextsIda

  val isBookSelected = BehaviorSubject(false)
  val numberOfBooks = BehaviorSubject(books.getList.size)
  books.onListData(numberOfBooks.onNext(books.getList.size))
  val isDirty = BehaviorSubject(false)
  def dirty = isDirty.onNext(true)
  def notDirty = isDirty.onNext(false)

  val component = new JScrollPane(new JPanelWithFrameLayout()
    .withBorders
    .withSeparators
    .newColumnFPG
    .newRowFPG
    .addInScrollPane(bookTable))

  Log.info("Loading file: " + file)

  private val library = Try(ExcelHandler.readLibrary(file))

  books.getList.clear
  library.foreach(lib => books.setList(lib.books))
  resetTableModel

  val previousBook = library.map(lib => Book(lib.columns.size)).getOrElse(Book(0))
  resetTableModel
  bookTable.setSelectionModel(new SingleListSelectionAdapter(books.getSelectionIndexHolder))
  bookTable.setEnabled(true)
  bookTable.setDefaultRenderer(classOf[Object], highlightRenderer)
  bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

  bookTable.onDoubleClick(editBook)
  bookTable.onValueChanged(isBookSelected.onNext(bookTable.getSelectedRow >= 0))

  def filter(textToFilter: String) = {
    bookTable.setRowFilter(new SpecialCharacterRowFilter(textToFilter))
    highlightRenderer.setHighlightText(textToFilter)
  }

  def deleteBook(parent: Component) = {
    if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog( // TODO DialogHelper.yesNo
      parent,
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

  private def resetTableModel() = {
    if (library.isFailure)
      bookTable.setEmptyText(ERROR_AT_READING_LIBRARY + ": " + library.failed.get.getMessage)
    else if (books.getSize > 0)
      bookTable.setEmptyText("Ilyen szűrőfeltételekkel nem található book.")
    else
      bookTable.setEmptyText("Nincs book a listában.")

    library.foreach(library => bookTable.setModel(LibraryListTableModel(books, library.columns.toList, library.configuration)))
  }

  def saveLibrary = {
    Log.info("Save " + file)
    library.foreach(library => Try { ExcelHandler.saveLibrary(file, library); notDirty }) // TODO: error alert if failed.
  }

  def saveAsLibrary(newFile: File) = {
    Log.info("SaveAs " + file)
    def confirmOverwrite = DialogHelper.yesNo(null, texts.SaveAsOverwriteConfirmText(newFile), texts.SaveAsOverwriteConfirmWindowTitle, texts.SaveAsOverwriteYes, texts.SaveAsOverwriteNo)
    library.foreach(library => Try { if (newFile.notExists || confirmOverwrite) ExcelHandler.saveLibrary(newFile, library) })
  }

  def createNewBook = library.foreach(library => {
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
  })

  def editBook = library.foreach(library => {
    val selectedBookIndex = bookTable.convertRowIndexToModel(bookTable.getSelectedRow)

    val bookControllerInput = new BookControllerInput(Book(books.getList.get(selectedBookIndex)), library.columns.toList, library.configuration, library.books.toList, false, file)
    val bookController = new BookController

    val editorDialog = DialogHelper.startModalDialog(component, bookController, bookControllerInput)

    if (editorDialog.isOk) {
      saveImages(bookController.getOutput)
      books.getList.set(selectedBookIndex, bookController.getOutput)
      books.fireSelectedContentsChanged
      dirty
    }
  })

  private def saveImages(book: Book) = library.foreach(library => {
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
  })

  private def savePreviousBook(book: Book) = library.foreach(library => {
    library.configuration.getRememberingColumns.foreach(colName => {
      val columnIndex = library.columns.indexOf(colName)
      Log.info("LibraryController.savePreviousBook")
      previousBook.setValue(columnIndex, book.getValue(columnIndex))
    })
  })

  private def newPreviousBook(size: Int): Book = {
    val newBook = Book(size)

    library.foreach(library => {
      library.configuration.getRememberingColumns.foreach(rememberingColumn => {
        val columnIndex = library.columns.indexOf(rememberingColumn)
        newBook.setValue(columnIndex, previousBook.getValue(columnIndex))
        Log.info("LibraryController.newPreviousBook remembering col " + columnIndex + " val:" + previousBook.getValue(columnIndex))
      })
    })

    newBook
  }
}