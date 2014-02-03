package eu.eyan.idakonyvtar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.unix4j.convert.StringConverters;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import com.google.common.base.Charsets;
import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.list.SelectionInList;

import eu.eyan.idakonyvtar.model.IdaKönyvtárModel;
import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.view.IdaKönyvtárView;

public class IdaKönyvtár
{
    private static final int DEFAULT_MAGASSÁG = 600;
    private static final int DEFAULT_SZÉLESSÉG = 1000;

    public static void main(final String[] args)
    {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        IdaKönyvtár idaKönyvtár = new IdaKönyvtár();
        frame.add(idaKönyvtár.getView());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(DEFAULT_SZÉLESSÉG, DEFAULT_MAGASSÁG);
        frame.setLocation((screenSize.width - DEFAULT_SZÉLESSÉG) / 2, (screenSize.height - DEFAULT_MAGASSÁG) / 2);
        frame.setJMenuBar(new IdaKönyvtárMenü(idaKönyvtár));
        frame.pack();
        frame.setVisible(true);
    }

    private final IdaKönyvtárView view = new IdaKönyvtárView();
    private final IdaKönyvtárModel model = new IdaKönyvtárModel();

    public IdaKönyvtár()
    {
        importExcel(new File("C:\\Users\\FA\\Desktop\\házikönyvtár.xls"));
        KönyvtárListaTableModel dataModel = new KönyvtárListaTableModel(model.könyvek);
        view.könyvTábla.setModel(dataModel);
        view.könyvTábla.setEnabled(true);
    }

    public static class KönyvtárListaTableModel extends AbstractTableAdapter<Könyv>
    {
        private static final long serialVersionUID = 1L;
        private final SelectionInList<Könyv> listModel;

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

    public Component getView()
    {
        return view.getComponent();
    }

    public void importExcel(final File file)
    {
        try
        {
            model.könyvek.getList().clear();
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("Cp1252");
            Sheet excelKönyvek = Workbook.getWorkbook(file, ws).getSheet(0);
            for (int aktuálisSor = 0; aktuálisSor < excelKönyvek.getRows(); aktuálisSor++)
            {
                Könyv könyv = new Könyv();
                for (int aktuálisOszlop = 0; aktuálisOszlop < excelKönyvek.getColumns(); aktuálisOszlop++)
                {
                    try
                    {
                        könyv.setOszlop(aktuálisOszlop, excelKönyvek.getCell(aktuálisOszlop, aktuálisSor).getContents());
                    }
                    catch (Exception e)
                    {
                        JOptionPane.showMessageDialog(null, e.getMessage());
                        return;
                    }

                }
                model.könyvek.getList().add(könyv);
            }

            System.out.println();
            System.out.println(excelKönyvek.getCell("B2").getContents());

        }
        catch (BiffException e)
        {
            JOptionPane.showMessageDialog(null, "Biff Hiba a beolvasásnál");
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, "Hiba a beolvasásnál");
        }

    }
}
