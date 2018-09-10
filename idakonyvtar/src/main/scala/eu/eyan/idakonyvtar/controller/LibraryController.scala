package eu.eyan.idakonyvtar.controller

import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File

import scala.collection.JavaConversions.asScalaBuffer

import com.jgoodies.binding.adapter.SingleListSelectionAdapter
import eu.eyan.idakonyvtar.controller.adapter.LibraryListTableModel
import eu.eyan.idakonyvtar.controller.input.BookControllerInput
import eu.eyan.idakonyvtar.controller.input.LibraryControllerInput
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.LibraryModel
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.idakonyvtar.util.ExcelHandler
import eu.eyan.idakonyvtar.util.LibraryException
import eu.eyan.idakonyvtar.view.LibraryMenuAndToolBar
import eu.eyan.idakonyvtar.view.LibraryView
import eu.eyan.log.Log
import eu.eyan.log.LogWindow
import eu.eyan.util.swing.HighlightRenderer
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.JFileChooserPlus.JFileChooserImplicit
import eu.eyan.util.swing.SpecialCharacterRowFilter
import eu.eyan.util.swing.SwingPlus.showErrorDialog
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import eu.eyan.util.swing.JMenuItemPlus.JMenuItemImplicit
import scala.collection.mutable.Map
import eu.eyan.idakonyvtar.model.ColumnConfigurations
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import javax.imageio.ImageIO
import eu.eyan.util.swing.SwingPlus
import rx.lang.scala.subjects.BehaviorSubject

class LibraryController extends IController[LibraryControllerInput, Void] {

  /*TODO private*/ val view = new LibraryView
  private val model = new LibraryModel
  /*TODO private*/ val highlightRenderer = new HighlightRenderer

  var previousBook: Book = null
  var loadedFile: File = null

  override def getOutput(): Void = null
  override def getComponentForFocus(): Component = ???// TODO delete menuAndToolBar.TOOLBAR_SEARCH

  @deprecated override def getTitle = "" 
  
  lazy val numberOfBooks = BehaviorSubject(model.books.getList.size)

  override def getView = {
    view.getComponent
    resetTableModel
    view.getBookTable.setSelectionModel(new SingleListSelectionAdapter(model.books.getSelectionIndexHolder))
    view.getBookTable.setEnabled(true)
    view.getBookTable.setDefaultRenderer(classOf[Object], highlightRenderer)
    view.getBookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    view.getComponent
  }

  private def resetTableModel() = {
    if (model.books.getSize > 0)
      view.getBookTable.setEmptyText("Ilyen szűrőfeltételekkel nem található book.")
    else
      view.getBookTable.setEmptyText("Nincs book a listában.")

    val dataModel = LibraryListTableModel(model.books, model.library.columns.toList, model.library.configuration)
    view.getBookTable.setModel(dataModel)
  }

  override def initData(input: LibraryControllerInput): Unit = {
    readLibrary(input.file)
    previousBook = Book(model.library.columns.size)
  }

  private def readLibrary(file: File) = {
    Log.info("Loading file: " + file)
    loadedFile = file
    try model.library = ExcelHandler.readLibrary(file)
    catch { case le: LibraryException => showErrorDialog("Hiba a beolvasáskor", le) }

    model.books.getList.clear()
    model.books.setList(model.library.books)
    resetTableModel()
  }

  def loadLibrary = new JFileChooser()
    .withCurrentDirectory(".")
    .withDialogTitle("Töltés")
    .withApproveButtonText("Töltés")
    .withFileFilter("xls", "Excel97 fájlok")
    .showAndHandleResult(view.getComponent/* JFrame*/ , selectedFile => {
      Log.info("selected file: " + selectedFile)
      readLibrary(selectedFile)
    })

  def saveLibrary = new JFileChooser(new File("."))
    .withDialogTitle("Mentés")
    .withApproveButtonText("Mentés")
    .withFileFilter("xls", "Excel97 fájlok")
    .showAndHandleResult(view.getComponent/* JFrame*/, selectedFile => {
      Log.info("Save " + selectedFile)
      try ExcelHandler.saveLibrary(selectedFile, model.library)
      catch { case le: LibraryException => Log.error(le) }
    })

  def createNewBook = {
    val bookController = new BookController

    val editorDialog = DialogHelper.startModalDialog(
      view.getComponent, bookController, new BookControllerInput(
        newPreviousBook(model.library.columns.size),
        model.library.columns.toList,
        model.library.configuration,
        model.books.getList.toList,
        true,
        loadedFile))

    if (editorDialog.isOk()) {
      saveImages(bookController.getOutput)
      model.books.getList.add(0, bookController.getOutput)
      savePreviousBook(bookController.getOutput)
      model.books.fireIntervalAdded(0, 0)
    }
  }

  def editBook = {
    val selectedBookIndex = view.getBookTable().convertRowIndexToModel(view.getBookTable().getSelectedRow)

    val bookController = new BookController
    val editorDialog = DialogHelper.startModalDialog(
      view.getComponent,
      bookController,
      new BookControllerInput(
        Book(model.books.getList.get(selectedBookIndex)),
        model.library.columns.toList,
        model.library.configuration,
        model.library.books.toList,
        false,
        loadedFile))

    if (editorDialog.isOk()) {
      saveImages(bookController.getOutput)
      model.books.getList.set(selectedBookIndex, bookController.getOutput)
      model.books.fireSelectedContentsChanged()
    }
  }

  def saveImages(book: Book) = {
    val columns = model.library.columns.toList
    val columnConfiguration = model.library.configuration
    for { i <- 0 until columns.size } {
      val imageName = book.getValue(i)
      val columnName = columns(i)
      val isPictureField = columnConfiguration.isTrue(columnName, ColumnConfigurations.PICTURE)
      if(imageName == "" && book.images.contains(i)){
        val dir = loadedFile.getParentFile
        val imagesDir = (loadedFile.getAbsolutePath+".images").asDir
        if(!imagesDir.exists) imagesDir.mkdir
        val imageFile = (imagesDir.getAbsolutePath+"\\IMG.JPG").asFile.generateNewNameIfExists()
        ImageIO.write(book.images.get(i).get, "JPG", imageFile)
        book.values(i) = imageFile.getName
      } else {
        // do nothing image already there
      }
    }
  }

  def deleteBook(parent: Component) = {
    if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(
      parent,
      "Biztosan törölni akarod?",
      "Törlés megerősítése",
      JOptionPane.YES_NO_OPTION,
      JOptionPane.QUESTION_MESSAGE,
      null,
      Array("Igen", "Nem"),
      "Nem")) {
      val selectionIndex = model.books.getSelectionIndex
      model.books.getList.remove(selectionIndex)
      model.books.fireIntervalRemoved(selectionIndex, selectionIndex)
    }
  }

  def initBindings(): Unit = {


    view.getBookTable().addMouseListener(new MouseAdapter() {
      override def mouseClicked(e: MouseEvent) = if (e.getClickCount == 2) editBook
    })



    model.books.addListDataListener(new ListDataListener() {
      def onNumberOfBooks = numberOfBooks.onNext(model.books.getList.size)
      //TODO refactor add listener and listlistener     SwingPlus.onIntervalRemoved(action)
      def intervalRemoved(e: ListDataEvent) = onNumberOfBooks
      def intervalAdded(e: ListDataEvent) = onNumberOfBooks
      def contentsChanged(e: ListDataEvent) = onNumberOfBooks
    })
  }

  private def savePreviousBook(book: Book) = {
    model.library.configuration.getRememberingColumns().foreach(colName => {
      val columnIndex = model.library.columns.indexOf(colName)
      Log.info("LibraryController.savePreviousBook")
      previousBook.setValue(columnIndex, book.getValue(columnIndex))
    })
  }

  private def newPreviousBook(size: Int): Book = {
    val newBook = Book(size)
    model.library.configuration.getRememberingColumns().foreach(rememberingColumn => {
      val columnIndex = model.library.columns.indexOf(rememberingColumn)
      newBook.setValue(columnIndex, previousBook.getValue(columnIndex))
      Log.info("LibraryController.newPreviousBook remembering col " + columnIndex + " val:" + previousBook.getValue(columnIndex))
    })
    newBook
  }
}