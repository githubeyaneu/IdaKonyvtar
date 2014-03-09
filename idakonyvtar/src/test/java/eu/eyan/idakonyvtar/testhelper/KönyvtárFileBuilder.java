package eu.eyan.idakonyvtar.testhelper;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import eu.eyan.idakonyvtar.util.ExcelKezelő;

public class KönyvtárFileBuilder
{
    private WritableWorkbook workbook = null;
    private List<String[]> sorok = newArrayList();
    private WritableSheet aktuálisSheet = null;
    File file = new File(System.currentTimeMillis() + ".xls");

    public KönyvtárFileBuilder()
    {
        try
        {
            workbook = Workbook.createWorkbook(file, ExcelKezelő.getWorkbookSettings());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public KönyvtárFileBuilder withOszlopok(String... oszlopok)
    {
        for (int i = 0; i < oszlopok.length; i++)
        {
            try
            {
                aktuálisSheet.addCell(new Label(i, 0, oszlopok[i]));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return this;
    }

    public KönyvtárFileBuilder withSor(String... sor)
    {
        sorok.add(sor);
        for (int i = 0; i < sor.length; i++)
        {
            try
            {
                aktuálisSheet.addCell(new Label(i, sorok.size(), sor[i]));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return this;
    }

    public File save()
    {
        try
        {
            workbook.write();
            workbook.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return file;
    }

    public KönyvtárFileBuilder withSheet(String sheetName)
    {
        aktuálisSheet = workbook.createSheet(sheetName, workbook.getNumberOfSheets());
        return this;
    }
}
