package eu.eyan.idakonyvtar.controller

import eu.eyan.idakonyvtar.controller.input.BookControllerInput.ISBN_ENABLED
import javax.swing.JFileChooser.APPROVE_OPTION
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.JOptionPane
import javax.swing.JToolBar
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.filechooser.FileNameExtensionFilter
import com.jgoodies.binding.adapter.SingleListSelectionAdapter
import eu.eyan.idakonyvtar.controller.adapter.LibraryListTableModel
import eu.eyan.idakonyvtar.controller.input.BookControllerInput
import eu.eyan.idakonyvtar.controller.input.LibraryControllerInput
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.LibraryModel
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.idakonyvtar.util.ExcelHandler
import eu.eyan.idakonyvtar.util.HighlightRenderer
import eu.eyan.idakonyvtar.util.OkCancelDialog
import eu.eyan.idakonyvtar.util.SpecialCharacterRowFilter
import eu.eyan.idakonyvtar.view.LibraryMenuAndToolBar
import eu.eyan.idakonyvtar.view.LibraryView
import eu.eyan.idakonyvtar.controller.LibraryController._
import scala.collection.JavaConversions._
import javax.swing.WindowConstants

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

  def getToolBar(): javax.swing.JToolBar = menuAndToolBar.getToolBar()
  def getOutput(): Void = null
  def getComponentForFocus(): java.awt.Component = menuAndToolBar.TOOLBAR_SEARCH
  def getTitle(): String = TITLE + TITLE_SEPARATOR + model.getBooks().getList().size() + TITLE_PIECES
  def saveLibrary(file: File) = ExcelHandler.saveLibrary(file, model.getLibrary());
  def refreshTitle() = SwingUtilities.getWindowAncestor(view.getComponent()).asInstanceOf[JFrame].setTitle(getTitle())
  def getMenuBar() = menuAndToolBar.getMenuBar()

  def getView(): java.awt.Component = {
    view.getComponent()
    resetTableModel()
    view.getBookTable().setSelectionModel(new SingleListSelectionAdapter(model.getBooks().getSelectionIndexHolder()))
    view.getBookTable().setEnabled(true)
    view.getBookTable().setDefaultRenderer(new Object().getClass, highlightRenderer)
    view.getBookTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    view.getComponent()
  }

  def resetTableModel() = {
    // FIXME: is it really neccessary????
    if (model.getBooks().getSize() > 0) {
      view.getBookTable().setEmptyText("Ilyen szűrőfeltételekkel nem található book.")
    } else {
      view.getBookTable().setEmptyText("Nincs book a listában.")
    }
    val dataModel = LibraryListTableModel(model.getBooks(), model.getLibrary().getColumns(), model.getLibrary().getConfiguration());
    view.getBookTable().setModel(dataModel)
  }

  def initData(input: LibraryControllerInput): Unit = {
    readLibrary(input.getFile());
    previousBook = new Book(model.getLibrary().getColumns().size())
  }

  def readLibrary(file: File) = {
    System.out.println("Loading file: " + file)
    model.setLibrary(ExcelHandler.readLibrary(file))
    model.getBooks().getList().clear()
    // FIXME heee? 2x in model
    model.getBooks().setList(model.getLibrary().getBooks())
    resetTableModel()
  }

  def initBindings(): Unit = {
    menuAndToolBar.MENU_EXCEL_LOAD.addActionListener(this)
    menuAndToolBar.MENU_EXCEL_SAVE.addActionListener(this)

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
        view.getBookTable().setRowFilter(new SpecialCharacterRowFilter(menuAndToolBar.TOOLBAR_SEARCH.getText()));
        highlightRenderer.setHighlightText(menuAndToolBar.TOOLBAR_SEARCH.getText());
      }
    })

    val frame = SwingUtilities.getRoot(view.getComponent()).asInstanceOf[JFrame]
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    frame.addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent) =
        if (DialogHelper.yesNo(frame, "Biztos ki akar lépni?", "Megerősítés")) frame.dispose()
    })

    model.getBooks().addListDataListener(new ListDataListener() {
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
      new BookControllerInput.Builder()
        .withBook(new Book(model.getBooks().getList().get(selectedBookIndex)))
        .withColumns(model.getLibrary().getColumns())
        .withColumnConfiguration(model.getLibrary().getConfiguration())
        .withBookList(model.getLibrary().getBooks())
        .build())
    if (editorDialog.isOk()) {
      model.getBooks().getList().set(selectedBookIndex, bookController.getOutput())
      // TODO: ugly: use selectioninlist...
      model.getBooks().fireSelectedContentsChanged()
    }
  }

  def actionPerformed(e: ActionEvent): Unit = {
    if (e.getSource() == menuAndToolBar.MENU_EXCEL_LOAD) {
      val jFileChooser = new JFileChooser(".")
      jFileChooser.setApproveButtonText("Töltés")
      jFileChooser.setFileFilter(new FileNameExtensionFilter("Excel97 fájlok", "xls"))
      if (jFileChooser.showOpenDialog(menuAndToolBar.MENU_EXCEL_LOAD) == APPROVE_OPTION)
        readLibrary(jFileChooser.getSelectedFile());
    }

    if (e.getSource() == menuAndToolBar.MENU_EXCEL_SAVE) {
      val jFileChooser = new JFileChooser(new File("."))
      jFileChooser.setApproveButtonText("Mentés")
      jFileChooser.setFileFilter(new FileNameExtensionFilter("Excel97 fájlok", "xls"))
      if (jFileChooser.showOpenDialog(menuAndToolBar.MENU_EXCEL_SAVE) == APPROVE_OPTION) {
        System.out.println("Save " + jFileChooser.getSelectedFile())
        saveLibrary(jFileChooser.getSelectedFile())
      }
    }

    if (e.getSource() == menuAndToolBar.TOOLBAR_NEW_BOOK) {
      val bookController = new BookController()

      val editorDialog = DialogHelper.startModalDialog(
        view.getComponent(), bookController, new BookControllerInput.Builder()
          .withBook(newPreviousBook(model.getLibrary().getColumns().size()))
          .withBookList(model.getBooks().getList())
          .withColumns(model.getLibrary().getColumns())
          .withIsbnEnabled(ISBN_ENABLED)
          .withColumnConfiguration(model.getLibrary().getConfiguration())
          .build())
      if (editorDialog.isOk()) {
        model.getBooks().getList().add(0, bookController.getOutput())
        savePreviousBook(bookController.getOutput())
        // TODO: ugly: use selectioninlist...
        model.getBooks().fireIntervalAdded(0, 0);
      }
    }

    if (e.getSource() == menuAndToolBar.TOOLBAR_BOOK_DELETE
      && JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(
        menuAndToolBar.TOOLBAR_BOOK_DELETE,
        "Biztosan törölni akarod?",
        "Törlés megerősítése",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        Array(YES, NO),
        NO)) {
      val selectionIndex = model.getBooks().getSelectionIndex()
      model.getBooks().getList().remove(selectionIndex)
      // TODO: ugly: use selectioninlist...
      model.getBooks().fireIntervalRemoved(selectionIndex, selectionIndex)
    }
  }

  def savePreviousBook(book: Book) = {
    previousBook = new Book(model.getLibrary().getColumns().size())
    model.getLibrary().getConfiguration().getRememberingColumns().foreach(x => {
      val columnIndex = model.getLibrary().getColumns().indexOf(x);
      previousBook.setValue(columnIndex, book.getValue(columnIndex))
    })
  }

  def newPreviousBook(size: Int): Book = {
    val newBook = new Book(size);
    model.getLibrary().getConfiguration().getRememberingColumns().foreach(rememberingColumn => {
      val columnIndex = model.getLibrary().getColumns().indexOf(rememberingColumn)
      newBook.setValue(columnIndex, previousBook.getValue(columnIndex))
    })
    newBook
  }
}