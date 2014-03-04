package eu.eyan.idakonyvtar.testhelper;

import static eu.eyan.idakonyvtar.util.ExcelKezelő.getWorkbookSettings;
import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ExcelAssert
{

    public static void assertExcelCella(File forrásfile, String sheetNév, int oszlop, int sor, String expected)
    {
        Workbook workbook = null;
        try
        {
            workbook = Workbook.getWorkbook(forrásfile, getWorkbookSettings());
            assertThat(workbook.getSheet(sheetNév).getCell(oszlop - 1, sor - 1).getContents()).isEqualTo(expected);
        }
        catch (BiffException | IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            workbook.close();
        }

    }

}
