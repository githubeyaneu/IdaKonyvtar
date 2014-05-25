package eu.eyan.idakonyvtar.controller;

import static eu.eyan.idakonyvtar.controller.input.BookControllerInput.ISBN_ENABLED;
import static javax.swing.JFileChooser.APPROVE_OPTION;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jgoodies.binding.adapter.SingleListSelectionAdapter;

import eu.eyan.idakonyvtar.controller.adapter.LibraryListTableModel;
import eu.eyan.idakonyvtar.controller.input.BookControllerInput;
import eu.eyan.idakonyvtar.controller.input.LibraryControllerInput;
import eu.eyan.idakonyvtar.model.Book;
import eu.eyan.idakonyvtar.model.LibraryModel;
import eu.eyan.idakonyvtar.util.DialogHelper;
import eu.eyan.idakonyvtar.util.ExcelHandler;
import eu.eyan.idakonyvtar.util.HighlightRenderer;
import eu.eyan.idakonyvtar.util.OkCancelDialog;
import eu.eyan.idakonyvtar.util.SpecialCharacterRowFilter;
import eu.eyan.idakonyvtar.view.LibraryMenuAndToolBar;
import eu.eyan.idakonyvtar.view.LibraryView;

public class LibraryController implements IControllerWithMenu<LibraryControllerInput, Void>, ActionListener
{

    public static final String NO = "Nem";
    public static final String YES = "Igen";
    public static final String TITLE = "IdaKönyvtár";
    private final LibraryMenuAndToolBar menuAndToolBar = new LibraryMenuAndToolBar();
    private final LibraryView view = new LibraryView();
    private final LibraryModel model = new LibraryModel();
    private Book previousBook;
    HighlightRenderer highlightRenderer = new HighlightRenderer();

    // FIXME: initview and getview are not the same!!
    @Override
    public Component getView()
    {
        view.getComponent();
        resetTableModel();
        view.getBookTable().setSelectionModel(new SingleListSelectionAdapter(model.getBooks().getSelectionIndexHolder()));
        view.getBookTable().setEnabled(true);
        view.getBookTable().setDefaultRenderer(Object.class, highlightRenderer);
        view.getBookTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return view.getComponent();
    }

    private void resetTableModel()
    {
        // FIXME: is it really neccessary????
        if (model.getBooks().getSize() > 0)
        {
            view.getBookTable().setEmptyText("Ilyen szűrőfeltételekkel nem található book.");
        }
        else
        {
            view.getBookTable().setEmptyText("Nincs book a listában.");
        }
        LibraryListTableModel dataModel = new LibraryListTableModel(model.getBooks(), model.getLibrary().getColumns(), model.getLibrary().getConfiguration());
        view.getBookTable().setModel(dataModel);
    }

    @Override
    public String getTitle()
    {
        return TITLE;
    }

    @Override
    public void initData(LibraryControllerInput input)
    {
        readLibrary(input.getFile());
        previousBook = new Book(model.getLibrary().getColumns().size());
    }

    private void readLibrary(File file)
    {
        System.out.println("Loading file: " + file);
        model.setLibrary(ExcelHandler.readLibrary(file));
        model.getBooks().getList().clear();
        // FIXME heee? 2x in model
        model.getBooks().setList(model.getLibrary().getBooks());
        resetTableModel();
    }

    private void saveLibrary(File file)
    {
        ExcelHandler.saveLibrary(file, model.getLibrary());
    }

    @Override
    public void initBindings()
    {
        menuAndToolBar.MENU_EXCEL_LOAD.addActionListener(this);
        menuAndToolBar.MENU_EXCEL_SAVE.addActionListener(this);

        menuAndToolBar.TOOLBAR_NEW_BOOK.addActionListener(this);
        menuAndToolBar.TOOLBAR_BOOK_DELETE.addActionListener(this);

        view.getBookTable().getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                menuAndToolBar.TOOLBAR_BOOK_DELETE.setEnabled(view.getBookTable().getSelectedRow() >= 0);
            }
        });

        view.getBookTable().addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    editBook();
                }
            }

        });

        menuAndToolBar.TOOLBAR_SEARCH.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                view.getBookTable().setRowFilter(new SpecialCharacterRowFilter(menuAndToolBar.TOOLBAR_SEARCH.getText()));
                highlightRenderer.setHighlightText(menuAndToolBar.TOOLBAR_SEARCH.getText());
            }
        });

        JFrame frame = (JFrame) SwingUtilities.getRoot(view.getComponent());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (DialogHelper.yesNo(frame, "Biztos ki akar lépni?", "Megerősítés"))
                {
                    frame.dispose();
                }
            }
        });
    }

    private void editBook()
    {
        BookController bookController = new BookController();
        int selectedBookIndex = view.getBookTable().convertRowIndexToModel(view.getBookTable().getSelectedRow());
        OkCancelDialog editorDialog = DialogHelper.startModalDialog(
                view.getComponent(),
                bookController,
                new BookControllerInput.Builder()
                        .withBook(new Book(model.getBooks().getList().get(selectedBookIndex)))
                        .withColumns(model.getLibrary().getColumns())
                        .withColumnConfiguration(model.getLibrary().getConfiguration())
                        .withBookList(model.getLibrary().getBooks())
                        .build());
        if (editorDialog.isOk())
        {
            model.getBooks().getList().set(selectedBookIndex, bookController.getOutput());
            // TODO: ugly: use selectioninlist...
            model.getBooks().fireSelectedContentsChanged();
        }
    }

    @Override
    public JMenuBar getMenuBar()
    {
        return menuAndToolBar.getMenuBar();
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        if (e.getSource() == menuAndToolBar.MENU_EXCEL_LOAD)
        {
            JFileChooser jFileChooser = new JFileChooser(".");
            jFileChooser.setApproveButtonText("Töltés");
            jFileChooser.setFileFilter(new FileNameExtensionFilter("Excel97 fájlok", "xls"));
            if (jFileChooser.showOpenDialog(menuAndToolBar.MENU_EXCEL_LOAD) == APPROVE_OPTION)
            {
                readLibrary(jFileChooser.getSelectedFile());
            }
        }

        if (e.getSource() == menuAndToolBar.MENU_EXCEL_SAVE)
        {
            JFileChooser jFileChooser = new JFileChooser(new File("."));
            jFileChooser.setApproveButtonText("Mentés");
            jFileChooser.setFileFilter(new FileNameExtensionFilter("Excel97 fájlok", "xls"));
            if (jFileChooser.showOpenDialog(menuAndToolBar.MENU_EXCEL_SAVE) == APPROVE_OPTION)
            {
                System.out.println("Save " + jFileChooser.getSelectedFile());
                saveLibrary(jFileChooser.getSelectedFile());
            }
        }

        if (e.getSource() == menuAndToolBar.TOOLBAR_NEW_BOOK)
        {
            BookController bookController = new BookController();

            OkCancelDialog editorDialog = DialogHelper.startModalDialog(
                    view.getComponent()
                    , bookController
                    , new BookControllerInput.Builder()
                            .withBook(newPreviousBook(model.getLibrary().getColumns().size()))
                            .withBookList(model.getBooks().getList())
                            .withColumns(model.getLibrary().getColumns())
                            .withIsbnEnabled(ISBN_ENABLED)
                            .withColumnConfiguration(model.getLibrary().getConfiguration())
                            .build());
            if (editorDialog.isOk())
            {
                model.getBooks().getList().add(0, bookController.getOutput());
                savePreviousBook(bookController.getOutput());
                // TODO: ugly: use selectioninlist...
                model.getBooks().fireIntervalAdded(0, 0);
            }
        }

        if (e.getSource() == menuAndToolBar.TOOLBAR_BOOK_DELETE)
        {
            if (JOptionPane.showOptionDialog(menuAndToolBar.TOOLBAR_BOOK_DELETE, "Biztosan törölni akarod?", "Törlés megerősítése", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] { YES, NO }, NO) == JOptionPane.OK_OPTION)
            {
                int selectionIndex = model.getBooks().getSelectionIndex();
                model.getBooks().getList().remove(selectionIndex);
                // TODO: ugly: use selectioninlist...
                model.getBooks().fireIntervalRemoved(selectionIndex, selectionIndex);
            }
        }
    }

    private void savePreviousBook(Book book)
    {
        previousBook = new Book(model.getLibrary().getColumns().size());
        for (String rememberingColumn : model.getLibrary().getConfiguration().getRememberingColumns())
        {
            int columnIndex = model.getLibrary().getColumns().indexOf(rememberingColumn);
            previousBook.setValue(columnIndex, book.getValue(columnIndex));
        }
    }

    private Book newPreviousBook(int size)
    {
        Book newBook = new Book(size);
        for (String rememberingColumn : model.getLibrary().getConfiguration().getRememberingColumns())
        {
            int columnIndex = model.getLibrary().getColumns().indexOf(rememberingColumn);
            newBook.setValue(columnIndex, previousBook.getValue(columnIndex));
        }
        return newBook;
    }

    @Override
    public Void getOutput()
    {
        return null;
    }

    @Override
    public JToolBar getToolBar()
    {
        return menuAndToolBar.getToolBar();
    }

    @Override
    public Component getComponentForFocus()
    {
        return menuAndToolBar.TOOLBAR_SEARCH;
    }
}