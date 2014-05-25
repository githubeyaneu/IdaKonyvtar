package eu.eyan.idakonyvtar.view;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import lombok.Getter;

import com.google.common.io.Resources;

public class LibraryMenuAndToolBar
{
    public static final String DELETE_BOOK = "Book törlése";
    public static final String ADD_NEW_BOOK = "Új book hozzáadása";
    public static final String FILTER = "Szűrés: ";
    public static final String SAVE_LIBRARY = "Library mentése";
    public static final String LOAD_LIBRARY = "Library betöltése";
    public static final String ISBN_SEARCH = "ISBN keresés";
    public static final String FILE = "Fájl";

    public final JMenuItem MENU_EXCEL_LOAD = new JMenuItem(LOAD_LIBRARY);
    public final JMenuItem MENU_EXCEL_SAVE = new JMenuItem(SAVE_LIBRARY);
    private final JMenu MENU_FILE = new JMenu(FILE);
    public final JButton TOOLBAR_NEW_BOOK = new JButton("Új book", new ImageIcon(Resources.getResource("icons/newbook.gif")));
    public final JButton TOOLBAR_BOOK_DELETE = new JButton("Törlés", new ImageIcon(Resources.getResource("icons/delete.gif")));
    final JLabel TOOLBAR_SEARCH_LABEL = new JLabel(FILTER);
    public final JTextField TOOLBAR_SEARCH = new JTextField(5);

    @Getter
    private final JToolBar toolBar = new JToolBar("Alapfunkciók");

    @Getter
    private final JMenuBar menuBar = new JMenuBar();

    public LibraryMenuAndToolBar()
    {
        super();
        menuBar.add(MENU_FILE);
        MENU_FILE.setName(FILE);

        MENU_FILE.add(MENU_EXCEL_LOAD);
        MENU_EXCEL_LOAD.setName(LOAD_LIBRARY);

        MENU_FILE.add(MENU_EXCEL_SAVE);
        MENU_EXCEL_SAVE.setName(SAVE_LIBRARY);

        toolBar.add(TOOLBAR_NEW_BOOK);
        TOOLBAR_NEW_BOOK.setName(ADD_NEW_BOOK);
        TOOLBAR_NEW_BOOK.setToolTipText(ADD_NEW_BOOK);

        toolBar.add(TOOLBAR_BOOK_DELETE);
        TOOLBAR_BOOK_DELETE.setToolTipText(DELETE_BOOK);
        TOOLBAR_BOOK_DELETE.setName(DELETE_BOOK);
        TOOLBAR_BOOK_DELETE.setEnabled(false);

        toolBar.add(TOOLBAR_SEARCH_LABEL);
        toolBar.add(TOOLBAR_SEARCH);
        TOOLBAR_SEARCH.setName(FILTER);
        TOOLBAR_SEARCH.setSize(200, TOOLBAR_SEARCH.getHeight());

    }
}
