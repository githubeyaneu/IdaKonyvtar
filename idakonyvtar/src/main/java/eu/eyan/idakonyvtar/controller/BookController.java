package eu.eyan.idakonyvtar.controller;

import static com.google.common.collect.Lists.newArrayList;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.fest.util.Objects;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.adapter.ComboBoxAdapter;

import eu.eyan.idakonyvtar.controller.input.BookControllerInput;
import eu.eyan.idakonyvtar.model.Book;
import eu.eyan.idakonyvtar.model.BookFieldValueModel;
import eu.eyan.idakonyvtar.model.ColumnKonfiguration.ColumnConfigurations;
import eu.eyan.idakonyvtar.oszk.Marc;
import eu.eyan.idakonyvtar.oszk.OszkKereso;
import eu.eyan.idakonyvtar.oszk.OszkKeresoException;
import eu.eyan.idakonyvtar.util.BookHelper;
import eu.eyan.idakonyvtar.view.BookView;
import eu.eyan.idakonyvtar.view.MultiField;
import eu.eyan.idakonyvtar.view.MultiFieldJComboBox;
import eu.eyan.idakonyvtar.view.MultiFieldJTextField;

public class BookController implements IDialogController<BookControllerInput, Book>
{
    private BookView view = new BookView();
    private BookControllerInput model;
    private List<Window> resizeListeners = newArrayList();

    @Override
    public Component getView()
    {
        return view.getComponent();
    }

    @Override
    public String getTitle()
    {
        if (model.getColumns().indexOf("Szerző") >= 0)
        {
            return "Book adatainak szerkesztése - " + model.getBook().getValue(model.getColumns().indexOf("Szerző"));
        }
        else
        {
            return "Book adatainak szerkesztése";
        }
    }

    @Override
    public void initData(BookControllerInput model)
    {
        this.model = model;
        view.setColumns(model.getColumns());
        view.setIsbnEnabled(model.isIsbnEnabled());
        view.setColumnConfiguration(model.getColumnConfiguration());
    }

    @Override
    public void initBindings()
    {
        initFieldsActionBindings();
        view.getIsbnText().addActionListener(isbnSearch());
    }

    private void initFieldsActionBindings()
    {
        for (int columnIndex = 0; columnIndex < model.getColumns().size(); columnIndex++)
        {
            String columnName = model.getColumns().get(columnIndex);
            boolean autoComplete = model.getColumnConfiguration().isTrue(columnName, ColumnConfigurations.AUTOCOMPLETE);
            boolean multi = model.getColumnConfiguration().isTrue(columnName, ColumnConfigurations.MULTIFIELD);
            if (autoComplete)
            {
                List<String> columnList = BookHelper.getColumnList(model.getBookList(), columnIndex);
                if (multi)
                {
                    MultiFieldJComboBox mmcombo = (MultiFieldJComboBox) view.getEditors().get(columnIndex);
                    mmcombo.setAutoCompleteList(columnList);
                    multimezőBind(mmcombo, new BookFieldValueModel(columnIndex, model.getBook()));
                }
                else
                {
                    JComboBox<?> comboBox = (JComboBox<?>) view.getEditors().get(columnIndex);
                    Bindings.bind(comboBox, new ComboBoxAdapter<String>(columnList, new BookFieldValueModel(columnIndex, model.getBook())));
                    AutoCompleteDecorator.decorate(comboBox);
                }
            }
            else
            {
                if (multi)
                {
                    MultiFieldJTextField mmc = (MultiFieldJTextField) view.getEditors().get(columnIndex);
                    multimezőBind(mmc, new BookFieldValueModel(columnIndex, model.getBook()));
                }
                else
                {
                    Bindings.bind((JTextField) view.getEditors().get(columnIndex), new BookFieldValueModel(columnIndex, model.getBook()));
                }
            }
        }
    }

    private void multimezőBind(final MultiField<String, ?> mmc, final BookFieldValueModel bookFieldValueModel)
    {
        bookFieldValueModel.addValueChangeListener((PropertyChangeEvent propertyChangeEvent) -> {
            if (!Objects.areEqual(propertyChangeEvent.getNewValue(), propertyChangeEvent.getOldValue()))
            {
                mmc.setValues(getMultiFieldList((String) propertyChangeEvent.getNewValue()));
            }
        });
        mmc.setValues(getMultiFieldList((String) bookFieldValueModel.getValue()));

        mmc.addPropertyChangeListener((PropertyChangeEvent propertyChangeEvent) -> {
            bookFieldValueModel.setValue(Joiner.on(BookHelper.LISTA_SEPARATOR).skipNulls().join(mmc.getValues()));
        });
    }

    private static List<String> getMultiFieldList(String value)
    {
        String[] strings = (value).split(BookHelper.LISTA_SEPARATOR_REGEX);
        List<String> list = newArrayList(strings).stream().filter((String s) -> {
            return !s.isEmpty();
        }).collect(Collectors.toList());
        return list;
    }

    private ActionListener isbnSearch()
    {
        return new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (e.getSource() == view.getIsbnText())
                {
                    System.out.println("ISBN Action: " + view.getIsbnText().getText());
                    view.getIsbnText().selectAll();
                    view.getIsbnSearchLabel().setText("Keresés");
                    view.getIsbnSearchLabel().setIcon(new ImageIcon(Resources.getResource("icons/search.gif")));
                    view.getEditors().forEach(component -> component.setEnabled(false));
                    // TODO Asynchron
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            List<Marc> marcsToIsbn;
                            try
                            {
                                marcsToIsbn = OszkKereso.getMarcsToIsbn(view.getIsbnText().getText().replaceAll("ö", "0"));
                                prozessIsbnData(marcsToIsbn);
                            }
                            catch (OszkKeresoException e)
                            {
                                // FIXME: itt fontos a naplózás
                                view.getIsbnSearchLabel().setText("Nincs találat");
                                view.getIsbnSearchLabel().setIcon(new ImageIcon(Resources.getResource("icons/error.gif")));
                            }
                            finally
                            {
                                view.getEditors().forEach(component -> component.setEnabled(true));
                                fireResizeEvent();
                            }
                        }
                    });
                    fireResizeEvent();
                }
            }
        };
    }

    private void prozessIsbnData(List<Marc> marcsToIsbn)
    {
        for (String column : model.getColumns())
        {
            String columnValue = "";
            List<Marc> marcCodesToColumns;
            try
            {
                marcCodesToColumns = model.getColumnConfiguration().getMarcCodes(column);
                for (Marc marc : marcsToIsbn)
                {
                    for (Marc columnMarc : marcCodesToColumns)
                    {
                        if (isMarcsApply(marc, columnMarc))
                        {
                            columnValue += columnValue.equals("") ? marc.getValue() : ", " + marc.getValue();
                        }
                    }
                }
                model.getBook().setValue(model.getColumns().indexOf(column), columnValue);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
            }
        }
    }

    private boolean isMarcsApply(Marc pontosMarc, Marc pontatlanMarc)
    {
        if (pontosMarc == null || pontatlanMarc == null || pontosMarc.getMarc1() == null || pontatlanMarc.getMarc1() == null)
        {
            return false;
        }
        if (pontosMarc.getMarc1().equalsIgnoreCase(pontatlanMarc.getMarc1()))
        {
            if (pontatlanMarc.getMarc2().equals("") || pontosMarc.getMarc2().equalsIgnoreCase(pontatlanMarc.getMarc2()))
            {
                if (pontatlanMarc.getMarc3().equals("") || pontosMarc.getMarc3().equalsIgnoreCase(pontatlanMarc.getMarc3()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onOk()
    {
    }

    @Override
    public void onCancel()
    {
    }

    @Override
    public Book getOutput()
    {
        return model.getBook();
    }

    @Override
    public Component getComponentForFocus()
    {
        return view.getIsbnText();
    }

    @Override
    public void addResizeListener(Window window)
    {
        this.resizeListeners.add(window);
    }

    private void fireResizeEvent()
    {
        for (Window window : resizeListeners)
        {
            window.pack();
        }
    }
}
