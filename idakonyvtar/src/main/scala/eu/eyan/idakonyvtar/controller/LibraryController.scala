package eu.eyan.idakonyvtar.controller

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import scala.collection.JavaConversions.asScalaBuffer
import com.jgoodies.binding.adapter.SingleListSelectionAdapter
import eu.eyan.idakonyvtar.controller.LibraryController.NO
import eu.eyan.idakonyvtar.controller.LibraryController.TITLE
import eu.eyan.idakonyvtar.controller.LibraryController.TITLE_PIECES
import eu.eyan.idakonyvtar.controller.LibraryController.TITLE_SEPARATOR
import eu.eyan.idakonyvtar.controller.LibraryController.YES
import eu.eyan.idakonyvtar.controller.adapter.LibraryListTableModel
import eu.eyan.idakonyvtar.controller.input.BookControllerInput
import eu.eyan.idakonyvtar.controller.input.LibraryControllerInput
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.LibraryModel
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.idakonyvtar.util.ExcelHandler
import eu.eyan.idakonyvtar.util.HighlightRenderer
import eu.eyan.idakonyvtar.util.LibraryException
import eu.eyan.idakonyvtar.util.SpecialCharacterRowFilter
import eu.eyan.idakonyvtar.view.LibraryMenuAndToolBar
import eu.eyan.idakonyvtar.view.LibraryView
import javax.swing.JFileChooser
import javax.swing.JFileChooser.APPROVE_OPTION
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.filechooser.FileNameExtensionFilter
import eu.eyan.idakonyvtar.model.Library
import eu.eyan.util.awt.AwtHelper._
import eu.eyan.log.LogWindow
import eu.eyan.log.Log

object LibraryController {
  val NO = "Nem"
  val TITLE = "IdaKönyvtár"
  val YES = "Igen"
  val TITLE_SEPARATOR = " - "
  val TITLE_PIECES = " db Könyv"
}

class LibraryController extends IControllerWithMenu[LibraryControllerInput, Void] with ActionListener {

  private val menuAndToolBar = new LibraryMenuAndToolBar
  private val view = new LibraryView
  private val model = new LibraryModel
  private val highlightRenderer = new HighlightRenderer

  var previousBook: Book = null

  def getToolBar() = menuAndToolBar.getToolBar()
  def getOutput(): Void = null
  def getComponentForFocus(): java.awt.Component = menuAndToolBar.TOOLBAR_SEARCH
  def getTitle() = TITLE + TITLE_SEPARATOR + model.books.getList().size() + TITLE_PIECES
  def refreshTitle() = SwingUtilities.getWindowAncestor(view.getComponent()).asInstanceOf[JFrame].setTitle(getTitle())
  def getMenuBar() = menuAndToolBar.getMenuBar()
  def saveLibrary(file: File) =
    try ExcelHandler.saveLibrary(file, model.library)
    catch {
      case le: LibraryException =>
    }

  def getView() = {
    view.getComponent()
    resetTableModel()
    view.getBookTable().setSelectionModel(new SingleListSelectionAdapter(model.books.getSelectionIndexHolder()))
    view.getBookTable().setEnabled(true)
    view.getBookTable().setDefaultRenderer(classOf[Object], highlightRenderer)
    view.getBookTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    view.getComponent()
  }

  private def resetTableModel() = {
    if (model.books.getSize() > 0)
      view.getBookTable().setEmptyText("Ilyen szűrőfeltételekkel nem található book.")
    else
      view.getBookTable().setEmptyText("Nincs book a listában.")

    val dataModel = LibraryListTableModel(model.books, model.library.columns /*FIXME*/ .toList, model.library.configuration);
    view.getBookTable().setModel(dataModel)
  }

  def initData(input: LibraryControllerInput): Unit = {
    readLibrary(input.file);
    previousBook = Book(model.library.columns.size)
  }

  private def readLibrary(file: File) = {
    Log.info("Loading file: " + file)
    try model.library = ExcelHandler.readLibrary(file)
    catch {
      case le: LibraryException => showErrorDialog("Hiba a beolvasáskor", le)
    }

    model.books.getList().clear()
    model.books.setList(model.library.books)
    resetTableModel()
  }

  private def showErrorDialog(msg: String, e: Throwable, shown: Set[Throwable] = Set()): Unit = {
    if (e.getCause != null && !shown.contains(e.getCause))
      showErrorDialog(msg + ", " + e.getLocalizedMessage, e.getCause, shown + e)
    else JOptionPane.showMessageDialog(null, msg + ", " + e.getLocalizedMessage)
  }

  def initBindings(): Unit = {
    menuAndToolBar.MENU_EXCEL_LOAD.addActionListener(this)
    menuAndToolBar.MENU_EXCEL_SAVE.addActionListener(this)
    menuAndToolBar.MENU_OPEN_DEBUG_WINDOW.addActionListener(newActionListener(e => { LogWindow.show() }))

    menuAndToolBar.TOOLBAR_NEW_BOOK.addActionListener(this)
    menuAndToolBar.TOOLBAR_BOOK_DELETE.addActionListener(this)

    view.getBookTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      def valueChanged(e: ListSelectionEvent) = menuAndToolBar.TOOLBAR_BOOK_DELETE.setEnabled(view.getBookTable().getSelectedRow() >= 0)
    })

    view.getBookTable().addMouseListener(new MouseAdapter() {
      override def mouseClicked(e: MouseEvent) = if (e.getClickCount() == 2) editBook()
    })

    menuAndToolBar.TOOLBAR_SEARCH.addKeyListener(new KeyAdapter() {
      override def keyReleased(e: KeyEvent) {
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

  def editBook() = {
    val bookController = new BookController()
    val selectedBookIndex = view.getBookTable().convertRowIndexToModel(view.getBookTable().getSelectedRow())
    val editorDialog = DialogHelper.startModalDialog(
      view.getComponent(),
      bookController,
      new BookControllerInput(
        Book(model.books.getList().get(selectedBookIndex)), model.library.columns /*FIXME*/ .toList, model.library.configuration, model.library.books /*FIXME*/ .toList));

    if (editorDialog.isOk()) {
      model.books.getList().set(selectedBookIndex, bookController.getOutput)
      model.books.fireSelectedContentsChanged()
    }
  }

  def actionPerformed(e: ActionEvent): Unit = {
    e.getSource match {
      case menuAndToolBar.MENU_EXCEL_LOAD =>
        val jFileChooser = new JFileChooser(".")
        jFileChooser.setApproveButtonText("Töltés")
        jFileChooser.setFileFilter(new FileNameExtensionFilter("Excel97 fájlok", "xls"))
        if (jFileChooser.showOpenDialog(menuAndToolBar.MENU_EXCEL_LOAD) == APPROVE_OPTION)
          readLibrary(jFileChooser.getSelectedFile())

      case menuAndToolBar.MENU_EXCEL_SAVE =>
        val jFileChooser = new JFileChooser(new File("."))
        jFileChooser.setApproveButtonText("Mentés")
        jFileChooser.setFileFilter(new FileNameExtensionFilter("Excel97 fájlok", "xls"))
        if (jFileChooser.showOpenDialog(menuAndToolBar.MENU_EXCEL_SAVE) == APPROVE_OPTION) {
          Log.info("Save " + jFileChooser.getSelectedFile())
          saveLibrary(jFileChooser.getSelectedFile())
        }

      case menuAndToolBar.TOOLBAR_NEW_BOOK =>
        val bookController = new BookController()
        val editorDialog = DialogHelper.startModalDialog(
          view.getComponent(), bookController, new BookControllerInput(
            newPreviousBook(model.library.columns.size), model.library.columns /*FIXME*/ .toList, model.library.configuration, model.books.getList() /*FIXME*/ .toList, true));
        if (editorDialog.isOk()) {
          model.books.getList().add(0, bookController.getOutput)
          savePreviousBook(bookController.getOutput)
          // TODO: ugly: use selectioninlist...
          model.books.fireIntervalAdded(0, 0);
        }

      case menuAndToolBar.TOOLBAR_BOOK_DELETE =>
        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(
          menuAndToolBar.TOOLBAR_BOOK_DELETE,
          "Biztosan törölni akarod?",
          "Törlés megerősítése",
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE,
          null,
          Array(YES, NO),
          NO)) {
          val selectionIndex = model.books.getSelectionIndex()
          model.books.getList().remove(selectionIndex)
          // TODO: ugly: use selectioninlist...
          model.books.fireIntervalRemoved(selectionIndex, selectionIndex)
        }
    }
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
      Log.info("LibraryController.newPreviousBook")
      newBook.setValue(columnIndex, previousBook.getValue(columnIndex))
    })
    newBook
  }

}