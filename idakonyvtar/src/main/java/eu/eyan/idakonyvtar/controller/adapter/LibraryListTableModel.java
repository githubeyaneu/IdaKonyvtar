package eu.eyan.idakonyvtar.controller.adapter;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.list.SelectionInList;

import eu.eyan.idakonyvtar.model.Book;
import eu.eyan.idakonyvtar.model.ColumnKonfiguration;
import eu.eyan.idakonyvtar.model.ColumnKonfiguration.ColumnConfigurations;

public class LibraryListTableModel extends AbstractTableAdapter<Book>
{
    private static final long serialVersionUID = 1L;
    private SelectionInList<Book> listModel;
    private static int[] showingColumnIndices;

    public Book getSelectedBook(int selectedBookIndex)
    {
        return listModel.getSelection();
    }

    public LibraryListTableModel(final SelectionInList<Book> listModel, List<String> columnok, ColumnKonfiguration columnConfiguration)
    {
        super(listModel, columnsToShow(columnok, columnConfiguration));
        this.listModel = listModel;
    }

//        @Override
//        public boolean isCellEditable(final int rowIndex, final int columnIndex)
//        {
//            return true;
//        }
//
//        @Override
//        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
//        {
//            try
//            {
//                listModel.getList().get(rowIndex).setColumn(columnIndex, (String) aValue);
//            }
//            catch (Exception e)
//            {
//                // Nem lehetséges elvileg
//            }
//        }

    private static String[] columnsToShow(List<String> everyColumn, ColumnKonfiguration columnConfiguration)
    {
        List<String> columnsToShow = newArrayList();
        List<Integer> columnsToShowIndexei = newArrayList();
        for (int i = 0; i < everyColumn.size(); i++)
        {
            if (columnConfiguration.isTrue(everyColumn.get(i), ColumnConfigurations.SHOW_IN_TABLE))
            {
                columnsToShow.add(everyColumn.get(i));
                columnsToShowIndexei.add(i);
            }
        }
        showingColumnIndices = new int[columnsToShowIndexei.size()];
        for (int i = 0; i < columnsToShowIndexei.size(); i++)
        {
            showingColumnIndices[i] = columnsToShowIndexei.get(i);
        }
        String[] columnsToShowArray = columnsToShow.toArray(new String[columnsToShow.size()]);
        if (columnsToShowArray.length < 1)
        {
            throw new IllegalArgumentException("Legalább 1 columnot meg kell jeleníteni! Columnconfigurationban helyesen kell konfigurálni.");
        }
        return columnsToShowArray;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex)
    {
        Book book = getRow(rowIndex);
        return book.getValue(showingColumnIndices[columnIndex]);
    }
}