package eu.eyan.idakonyvtar.controller.adapter;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.list.SelectionInList;

import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.model.OszlopKonfiguráció;
import eu.eyan.idakonyvtar.model.OszlopKonfiguráció.OszlopKonfigurációk;

public class KönyvtárListaTableModel extends AbstractTableAdapter<Könyv>
{
    private static final long serialVersionUID = 1L;
    private SelectionInList<Könyv> listModel;
    private List<String> összesOszlop;
    private static int[] megjelenítettOszlopIndexek;

    public Könyv getSelectedKönyv(int selectedKönyvIndex)
    {
        return listModel.getSelection();
    }

    public KönyvtárListaTableModel(final SelectionInList<Könyv> listModel, List<String> oszlopok, OszlopKonfiguráció oszlopKonfiguráció)
    {
        super(listModel, megjelenítendőOszlopok(oszlopok, oszlopKonfiguráció));
        this.listModel = listModel;
        this.összesOszlop = oszlopok;
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

    private static String[] megjelenítendőOszlopok(List<String> összesOszlop, OszlopKonfiguráció oszlopKonfiguráció)
    {
        List<String> megjelenítendőOszlopok = newArrayList();
        List<Integer> megjelenítendőOszlopokIndexei = newArrayList();
        for (int i = 0; i < összesOszlop.size(); i++)
        {
            if (oszlopKonfiguráció.isIgen(összesOszlop.get(i), OszlopKonfigurációk.MEGJELENÍTÉS_TÁBLÁZATBAN))
            {
                megjelenítendőOszlopok.add(összesOszlop.get(i));
                megjelenítendőOszlopokIndexei.add(i);
            }
        }
        megjelenítettOszlopIndexek = new int[megjelenítendőOszlopokIndexei.size()];
        for (int i = 0; i < megjelenítendőOszlopokIndexei.size(); i++)
        {
            megjelenítettOszlopIndexek[i] = megjelenítendőOszlopokIndexei.get(i);
        }
        return megjelenítendőOszlopok.toArray(new String[megjelenítendőOszlopok.size()]);
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex)
    {
        Könyv könyv = getRow(rowIndex);
        return könyv.getValue(megjelenítettOszlopIndexek[columnIndex]);
    }
}