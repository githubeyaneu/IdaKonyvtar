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
import eu.eyan.idakonyvtar.text.Texts.ERROR_AT_READING
import eu.eyan.idakonyvtar.text.Texts.NO
import eu.eyan.idakonyvtar.text.Texts.NO_BOOK_FOR_THE_FILTER
import eu.eyan.idakonyvtar.text.Texts.NO_BOOK_IN_THE_LIST
import eu.eyan.idakonyvtar.text.Texts.TITLE
import eu.eyan.idakonyvtar.text.Texts.TITLE_PIECES
import eu.eyan.idakonyvtar.text.Texts.TITLE_SEPARATOR
import eu.eyan.idakonyvtar.text.Texts.YES
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

class LibraryController extends IControllerWithMenu[LibraryControllerInput, Void] {

  private val menuAndToolBar = new LibraryMenuAndToolBar
  private val view = new LibraryView
  private val model = new LibraryModel
  private val highlightRenderer = new HighlightRenderer

  var previousBook: Book = null

  override def getToolBar() = menuAndToolBar.getToolBar()
  override def getOutput(): Void = null
  override def getComponentForFocus(): Component = menuAndToolBar.TOOLBAR_SEARCH
  override def getTitle() = TITLE + TITLE_SEPARATOR + model.books.getList.size() + TITLE_PIECES
  def refreshTitle() = SwingUtilities.getWindowAncestor(view.getComponent()).asInstanceOf[JFrame].setTitle(getTitle())
  override def getMenuBar() = menuAndToolBar.getMenuBar()

  override def getView() = {
    view.getComponent()
    resetTableModel()
    view.getBookTable().setSelectionModel(new SingleListSelectionAdapter(model.books.getSelectionIndexHolder))
    view.getBookTable().setEnabled(true)
    view.getBookTable().setDefaultRenderer(classOf[Object], highlightRenderer)
    view.getBookTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    view.getComponent()
  }

  private def resetTableModel() = {
    if (model.books.getSize > 0)
      view.getBookTable().setEmptyText(NO_BOOK_FOR_THE_FILTER)
    else
      view.getBookTable().setEmptyText(NO_BOOK_IN_THE_LIST)

    val dataModel = LibraryListTableModel(model.books, model.library.columns.toList, model.library.configuration)
    view.getBookTable().setModel(dataModel)
  }

  override def initData(input: LibraryControllerInput): Unit = {
    readLibrary(input.file)
    previousBook = Book(model.library.columns.size)
  }

  private def readLibrary(file: File) = {
    Log.info("Loading file: " + file)
    try model.library = ExcelHandler.readLibrary(file)
    catch { case le: LibraryException => showErrorDialog(ERROR_AT_READING, le) }

    model.books.getList.clear()
    model.books.setList(model.library.books)
    resetTableModel()
  }

  val loadLibrary = () => new JFileChooser()
    .withCurrentDirectory(".")
    .withDialogTitle("Töltés")
    .withApproveButtonText("Töltés")
    .withFileFilter("xls", "Excel97 fájlok")
    .showAndHandleResult(menuAndToolBar.MENU_EXCEL_LOAD, selectedFile => {
      Log.info("selected file: " + selectedFile)
      readLibrary(selectedFile)
    })

  val saveLibrary = () => new JFileChooser(new File("."))
    .withDialogTitle("Mentés")
    .withApproveButtonText("Mentés")
    .withFileFilter("xls", "Excel97 fájlok")
    .showAndHandleResult(menuAndToolBar.MENU_EXCEL_LOAD, selectedFile => {
      Log.info("Save " + selectedFile)
      try ExcelHandler.saveLibrary(selectedFile, model.library)
      catch { case le: LibraryException => Log.error(le) }
    })

  val createNewBook = () => {
    val bookController = new BookController

    val editorDialog = DialogHelper.startModalDialog(
      view.getComponent(), bookController, new BookControllerInput(
        newPreviousBook(model.library.columns.size),
        model.library.columns.toList,
        model.library.configuration,
        model.books.getList.toList,
        true))

    if (editorDialog.isOk()) {
      model.books.getList.add(0, bookController.getOutput)
      savePreviousBook(bookController.getOutput)
      model.books.fireIntervalAdded(0, 0)
    }
  }

  def editBook = {
    val selectedBookIndex = view.getBookTable().convertRowIndexToModel(view.getBookTable().getSelectedRow)

    val bookController = new BookController
    val editorDialog = DialogHelper.startModalDialog(
      view.getComponent(),
      bookController,
      new BookControllerInput(
        Book(model.books.getList.get(selectedBookIndex)),
        model.library.columns.toList,
        model.library.configuration,
        model.library.books.toList,
        false))

    if (editorDialog.isOk()) {
      model.books.getList.set(selectedBookIndex, bookController.getOutput)
      model.books.fireSelectedContentsChanged()
    }
  }

  val deleteBook = () => {
    if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(
      menuAndToolBar.TOOLBAR_BOOK_DELETE,
      "Biztosan törölni akarod?",
      "Törlés megerősítése",
      JOptionPane.YES_NO_OPTION,
      JOptionPane.QUESTION_MESSAGE,
      null,
      Array(YES, NO),
      NO)) {
      val selectionIndex = model.books.getSelectionIndex
      model.books.getList.remove(selectionIndex)
      model.books.fireIntervalRemoved(selectionIndex, selectionIndex)
    }
  }

  def initBindings(): Unit = {

    menuAndToolBar.TOOLBAR_LOAD.onAction(loadLibrary)
    menuAndToolBar.MENU_EXCEL_LOAD.onAction(loadLibrary)

    menuAndToolBar.TOOLBAR_SAVE.onAction(saveLibrary)
    menuAndToolBar.MENU_EXCEL_SAVE.onAction(saveLibrary)

    menuAndToolBar.TOOLBAR_NEW_BOOK.onAction(createNewBook)

    menuAndToolBar.TOOLBAR_BOOK_DELETE.onAction(deleteBook)

    menuAndToolBar.MENU_OPEN_DEBUG_WINDOW.onAction(() => LogWindow.show(SwingUtilities.windowForComponent(getView)))

    view.getBookTable.getSelectionModel.addListSelectionListener(new ListSelectionListener() {
      def valueChanged(e: ListSelectionEvent) = menuAndToolBar.TOOLBAR_BOOK_DELETE.setEnabled(view.getBookTable().getSelectedRow >= 0)
    })

    view.getBookTable().addMouseListener(new MouseAdapter() {
      override def mouseClicked(e: MouseEvent) = if (e.getClickCount == 2) editBook
    })

    menuAndToolBar.TOOLBAR_SEARCH.addKeyListener(new KeyAdapter() {
      override def keyReleased(e: KeyEvent) = {
        view.getBookTable().setRowFilter(new SpecialCharacterRowFilter(menuAndToolBar.TOOLBAR_SEARCH.getText()))
        highlightRenderer.setHighlightText(menuAndToolBar.TOOLBAR_SEARCH.getText())
      }
    })

    val frame = SwingUtilities.getRoot(view.getComponent()).asInstanceOf[JFrame]
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    frame.addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent) =
        if (DialogHelper.yesNo(frame, "Biztos ki akar lépni?", "Megerősítés")) frame.dispose()
    })

    model.books.addListDataListener(new ListDataListener() {
      def intervalRemoved(e: ListDataEvent) = refreshTitle()
      def intervalAdded(e: ListDataEvent) = refreshTitle()
      def contentsChanged(e: ListDataEvent) = refreshTitle()
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