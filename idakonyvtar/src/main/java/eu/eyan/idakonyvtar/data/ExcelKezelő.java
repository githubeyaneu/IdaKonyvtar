package eu.eyan.idakonyvtar.data;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import eu.eyan.idakonyvtar.model.Könyv;

public class ExcelKezelő
{
    public static List<Könyv> könyvtárBeolvasása(final File file)
    {
        List<Könyv> könyvek = newArrayList();
        try
        {
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("Cp1252");
            Sheet excelKönyvek = Workbook.getWorkbook(file, ws).getSheet(0);
            for (int aktuálisSor = 1; aktuálisSor < excelKönyvek.getRows(); aktuálisSor++)
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
                        return könyvek;
                    }
                }
                könyvek.add(könyv);
            }
        }
        catch (BiffException e)
        {
            JOptionPane.showMessageDialog(null, "Biff Hiba a beolvasásnál");
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, "Hiba a beolvasásnál");
        }
        return könyvek;
    }
}
