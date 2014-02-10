package eu.eyan.idakonyvtar.controller.adapter;

import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.list.SelectionInList;

import eu.eyan.idakonyvtar.model.Könyv;

public class KönyvtárListaTableModel extends AbstractTableAdapter<Könyv>
{
    private static final long serialVersionUID = 1L;
    private SelectionInList<Könyv> listModel;

    public Könyv getSelectedKönyv()
    {
        return listModel.getSelection();
    }

    public KönyvtárListaTableModel(final SelectionInList<Könyv> listModel)
    {
        super(listModel, new String[] {
                "Szerző",
                "Szerző megj.",
                "Cím",
                "Kiadás",
                "Megjelenés helye",
                "Kiadó",
                "Év",
                "Terjedelem",
                "Ár",
                "Sorozat",
                "Téma",
                "Megjegyzés",
                "ISBN"
        });
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
//                listModel.getList().get(rowIndex).setOszlop(columnIndex, (String) aValue);
//            }
//            catch (Exception e)
//            {
//                // Nem lehetséges elvileg
//            }
//        }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex)
    {
        Könyv könyv = getRow(rowIndex);
        return könyv.getOszlop(columnIndex);
    }
}