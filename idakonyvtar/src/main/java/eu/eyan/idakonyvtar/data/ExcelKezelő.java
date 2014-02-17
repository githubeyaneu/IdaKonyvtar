package eu.eyan.idakonyvtar.data;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.io.FileUtils;

import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.model.Könyvtár;
import eu.eyan.idakonyvtar.util.BackupHelper;

public class ExcelKezelő
{
    public static Könyvtár könyvtárBeolvasása(final File file)
    {
        backup(file);
        Könyvtár könyvtár = new Könyvtár();
        try
        {
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("Cp1252");
            Sheet excelKönyvek = Workbook.getWorkbook(file, ws).getSheet(0);

            for (int aktuálisOszlop = 0; aktuálisOszlop < excelKönyvek.getColumns(); aktuálisOszlop++)
            {
                könyvtár.getOszlopok().add(excelKönyvek.getCell(aktuálisOszlop, 0).getContents());
            }

            for (int aktuálisSor = 1; aktuálisSor < excelKönyvek.getRows(); aktuálisSor++)
            {
                Könyv könyv = new Könyv(excelKönyvek.getColumns() + 1);
                for (int aktuálisOszlop = 0; aktuálisOszlop < excelKönyvek.getColumns(); aktuálisOszlop++)
                {
                    try
                    {
                        könyv.setValue(aktuálisOszlop, excelKönyvek.getCell(aktuálisOszlop, aktuálisSor).getContents());
                    }
                    catch (Exception e)
                    {
                        JOptionPane.showMessageDialog(null, e.getMessage());
                        e.printStackTrace();
                        return könyvtár;
                    }
                }
                könyvtár.getKönyvek().add(könyv);
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
        return könyvtár;
    }

    public static void könyvtárMentése(File célFile, Könyvtár könyvtár)
    {
        if (célFile.exists() && !célFile.isFile())
        {
            System.out.println("nem File");
            return;
        }

        if (célFile.exists())
        {
            backup(célFile);
            try
            {
                FileUtils.forceDelete(célFile);
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try
        {
            WritableWorkbook workbook = Workbook.createWorkbook(célFile);
            WritableSheet writablesheet1 = workbook.createSheet("Könyvek", 0);
            for (int oszlopIndex = 0; oszlopIndex < könyvtár.getOszlopok().size(); oszlopIndex++)
            {
                writablesheet1.addCell(new Label(oszlopIndex, 0, könyvtár.getOszlopok().get(oszlopIndex)));
                for (int könyvIndex = 0; könyvIndex < könyvtár.getKönyvek().size(); könyvIndex++)
                {
                    WritableCellFormat cellFormat = new WritableCellFormat();
                    cellFormat.setWrap(true);
                    writablesheet1.addCell(new Label(oszlopIndex, könyvIndex + 1, könyvtár.getKönyvek().get(könyvIndex).getValue(oszlopIndex), cellFormat));
                }
            }
            workbook.write();
            workbook.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (RowsExceededException e)
        {
            e.printStackTrace();
        }
        catch (WriteException e)
        {
            e.printStackTrace();
        }
    }

    private static void backup(File mentendőFile)
    {
        BackupHelper.zipFile(mentendőFile, new File(mentendőFile.getAbsolutePath() + "_backup_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".zip"));
    }
}
