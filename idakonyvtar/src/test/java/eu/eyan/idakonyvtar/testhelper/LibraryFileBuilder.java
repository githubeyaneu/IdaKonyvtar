package eu.eyan.idakonyvtar.testhelper;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import eu.eyan.idakonyvtar.util.ExcelHandler;

public class LibraryFileBuilder
{
    private WritableWorkbook workbook = null;
    private List<String[]> sorok = newArrayList();
    private WritableSheet actualSheet = null;
    File file = new File(System.currentTimeMillis() + ".xls");

    public LibraryFileBuilder()
    {
        try
        {
            workbook = Workbook.createWorkbook(file, ExcelHandler.getWorkbookSettings());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public LibraryFileBuilder withColumns(String... columns)
    {
        for (int i = 0; i < columns.length; i++)
        {
            try
            {
                actualSheet.addCell(new Label(i, 0, columns[i]));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return this;
    }

    public LibraryFileBuilder withRow(String... row)
    {
        sorok.add(row);
        for (int i = 0; i < row.length; i++)
        {
            try
            {
                actualSheet.addCell(new Label(i, sorok.size(), row[i]));
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

    public LibraryFileBuilder withSheet(String sheetName)
    {
        actualSheet = workbook.createSheet(sheetName, workbook.getNumberOfSheets());
        return this;
    }
}
