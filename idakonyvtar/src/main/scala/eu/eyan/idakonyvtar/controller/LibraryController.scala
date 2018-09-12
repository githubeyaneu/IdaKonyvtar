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
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.idakonyvtar.util.ExcelHandler
import eu.eyan.idakonyvtar.util.LibraryException
import eu.eyan.idakonyvtar.view.BookTable
import eu.eyan.log.Log
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.jgoodies.SelectionInListPlus.SelectionInListImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.swing.HighlightRenderer
import eu.eyan.util.swing.JFileChooserPlus.JFileChooserImplicit
import eu.eyan.util.swing.JPanelWithFrameLayout
import eu.eyan.util.swing.JTablePlus.JTableImplicit
import eu.eyan.util.swing.SpecialCharacterRowFilter
import eu.eyan.util.swing.SwingPlus.showErrorDialog
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.ListSelectionModel
import rx.lang.scala.subjects.BehaviorSubject

class LibraryController(file: File) {
	private val bookTable = new BookTable
	private val highlightRenderer = new HighlightRenderer
  private val books: SelectionInList[Book] = new SelectionInList[Book]()
  private var library: Library = null  // TODO /* var, because of loading other library */
  private var loadedFile: File = null

  val isBookSelected = BehaviorSubject(false)
  val numberOfBooks = BehaviorSubject(books.getList.size)

  val component = new JPanelWithFrameLayout()
    .withBorders
    .withSeparators
    .newColumnFPG
    .newRowFPG
    .addInScrollPane(bookTable)

  readLibrary(file)
  var previousBook: Book = Book(library.columns.size)
  resetTableModel
  bookTable.setSelectionModel(new SingleListSelectionAdapter(books.getSelectionIndexHolder))
  bookTable.setEnabled(true)
  bookTable.setDefaultRenderer(classOf[Object], highlightRenderer)
  bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

  bookTable.onDoubleClick(editBook)
  books.onListData(numberOfBooks.onNext(books.getList.size))
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
    }
  }
	  
	  
  private def resetTableModel() = {
    if (books.getSize > 0)
      bookTable.setEmptyText("Ilyen szűrőfeltételekkel nem található book.")
    else
      bookTable.setEmptyText("Nincs book a listában.")

    val dataModel = LibraryListTableModel(books, library.columns.toList, library.configuration)
    bookTable.setModel(dataModel)
  }

  private def readLibrary(file: File) = {
    Log.info("Loading file: " + file)
    loadedFile = file
    try library = ExcelHandler.readLibrary(file)
    catch { case le: LibraryException => showErrorDialog("Hiba a beolvasáskor", le) }

    books.getList.clear()
    books.setList(library.books)
    resetTableModel()
  }

  def loadLibrary = new JFileChooser()
    .withCurrentDirectory(".")
    .withDialogTitle("Töltés")
    .withApproveButtonText("Töltés")
    .withFileFilter("xls", "Excel97 fájlok")
    .showAndHandleResult(component /* JFrame*/ , selectedFile => {
      Log.info("selected file: " + selectedFile)
      readLibrary(selectedFile)
    })

  def saveLibrary = new JFileChooser(new File("."))
    .withDialogTitle("Mentés")
    .withApproveButtonText("Mentés")
    .withFileFilter("xls", "Excel97 fájlok")
    .showAndHandleResult(component /* JFrame*/ , selectedFile => {
      Log.info("Save " + selectedFile)
      try ExcelHandler.saveLibrary(selectedFile, library)
      catch { case le: LibraryException => Log.error(le) }
    })

  def createNewBook = {
    val bookController = new BookController

    val editorDialog = DialogHelper.startModalDialog(
      component, bookController, new BookControllerInput(
      newPreviousBook(library.columns.size),
      library.columns.toList,
      library.configuration,
      books.getList.toList,
      true,
      loadedFile))

    if (editorDialog.isOk()) {
      saveImages(bookController.getOutput)
      books.getList.add(0, bookController.getOutput)
      savePreviousBook(bookController.getOutput)
      books.fireIntervalAdded(0, 0)
    }
  }

  def editBook = {
    val selectedBookIndex = bookTable.convertRowIndexToModel(bookTable.getSelectedRow)

    val bookController = new BookController
    val editorDialog = DialogHelper.startModalDialog(
      component,
      bookController,
      new BookControllerInput(
        Book(books.getList.get(selectedBookIndex)),
        library.columns.toList,
        library.configuration,
        library.books.toList,
        false,
        loadedFile))

    if (editorDialog.isOk()) {
      saveImages(bookController.getOutput)
      books.getList.set(selectedBookIndex, bookController.getOutput)
      books.fireSelectedContentsChanged()
    }
  }

  private def saveImages(book: Book) = {
    val columns = library.columns.toList
    val columnConfiguration = library.configuration
    for { i <- 0 until columns.size } {
      val imageName = book.getValue(i)
      val columnName = columns(i)
      val isPictureField = columnConfiguration.isTrue(columnName, ColumnConfigurations.PICTURE)
      if (imageName == "" && book.images.contains(i)) {
        val dir = loadedFile.getParentFile
        val imagesDir = (loadedFile.getAbsolutePath + ".images").asDir
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
    library.configuration.getRememberingColumns().foreach(colName => {
      val columnIndex = library.columns.indexOf(colName)
      Log.info("LibraryController.savePreviousBook")
      previousBook.setValue(columnIndex, book.getValue(columnIndex))
    })
  }

  private def newPreviousBook(size: Int): Book = {
    val newBook = Book(size)
    library.configuration.getRememberingColumns().foreach(rememberingColumn => {
      val columnIndex = library.columns.indexOf(rememberingColumn)
      newBook.setValue(columnIndex, previousBook.getValue(columnIndex))
      Log.info("LibraryController.newPreviousBook remembering col " + columnIndex + " val:" + previousBook.getValue(columnIndex))
    })
    newBook
  }
}