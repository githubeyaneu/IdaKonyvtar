package eu.eyan.idakonyvtar.view;

import com.google.common.io.Resources

import LibraryMenuAndToolBar.ADD_NEW_BOOK
import LibraryMenuAndToolBar.FILTER
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JTextField
import javax.swing.JToolBar

object LibraryMenuAndToolBar {
  val ADD_NEW_BOOK = "Új book hozzáadása"
  val FILTER = "Szűrés: "
  val ISBN_SEARCH = "ISBN keresés"
}

class LibraryMenuAndToolBar {
  val SEARCH_TEXTFIELD_SIZE = 5
  val SEARCH_TOOLBAR_SIZE = 200
  private val toolBar = new JToolBar("Alapfunkciók")

  def getToolBar() = toolBar

  val TOOLBAR_SAVE = new JButton("Mentés", new ImageIcon(Resources.getResource("icons/save.gif")))
  val TOOLBAR_LOAD = new JButton("Töltés", new ImageIcon(Resources.getResource("icons/load.gif")))
  val TOOLBAR_NEW_BOOK = new JButton("Új book", new ImageIcon(Resources.getResource("icons/newbook.gif")))
  val TOOLBAR_BOOK_DELETE = new JButton("Törlés", new ImageIcon(Resources.getResource("icons/delete.gif")))
  val TOOLBAR_SEARCH = new JTextField(SEARCH_TEXTFIELD_SIZE)

  private val TOOLBAR_SEARCH_LABEL = new JLabel(FILTER)


  toolBar.add(TOOLBAR_SAVE)
  TOOLBAR_SAVE.setName("Library mentése")
  TOOLBAR_SAVE.setToolTipText("Library mentése")

  toolBar.add(TOOLBAR_LOAD)
  TOOLBAR_LOAD.setName("Library betöltése")
  TOOLBAR_LOAD.setToolTipText("Library betöltése")

  toolBar.add(TOOLBAR_NEW_BOOK)
  TOOLBAR_NEW_BOOK.setName(ADD_NEW_BOOK)
  TOOLBAR_NEW_BOOK.setToolTipText(ADD_NEW_BOOK)

  toolBar.add(TOOLBAR_BOOK_DELETE)
  TOOLBAR_BOOK_DELETE.setToolTipText("Book törlése")
  TOOLBAR_BOOK_DELETE.setName("Book törlése")
  TOOLBAR_BOOK_DELETE.setEnabled(false)

  toolBar.add(TOOLBAR_SEARCH_LABEL)
  toolBar.add(TOOLBAR_SEARCH)
  TOOLBAR_SEARCH.setName(FILTER)
  TOOLBAR_SEARCH.setSize(SEARCH_TOOLBAR_SIZE, TOOLBAR_SEARCH.getHeight)
}