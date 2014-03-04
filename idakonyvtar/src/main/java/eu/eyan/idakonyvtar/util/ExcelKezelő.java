package eu.eyan.idakonyvtar.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
import org.apache.commons.io.FilenameUtils;

import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.model.Könyvtár;

public class ExcelKezelő
{
    public static final String OSZLOP_KONFIGURÁCIÓ = "OszlopKonfiguráció";
    public static final String KÖNYVEK = "Könyvek";

    public static Könyvtár könyvtárBeolvasása(final File file)
    {
        backup(file);
        Könyvtár könyvtár = new Könyvtár();
        try
        {
            Workbook workbook = Workbook.getWorkbook(file, getWorkbookSettings());
            könyvekBeolvasása(könyvtár, getSheet(getWorkbookSettings(), workbook, KÖNYVEK));
            oszlopKonfigurációBeolvasása(könyvtár, getSheet(getWorkbookSettings(), workbook, OSZLOP_KONFIGURÁCIÓ));
        }
        catch (BiffException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Biff Hiba a beolvasásnál " + e.getLocalizedMessage());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Hiba a beolvasásnál " + e.getLocalizedMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
        }
        return könyvtár;
    }

    public static WorkbookSettings getWorkbookSettings()
    {
        WorkbookSettings ws = new WorkbookSettings();
        ws.setEncoding("Cp1252");
        return ws;
    }

    private static Sheet getSheet(WorkbookSettings ws, Workbook workbook, String string)
    {
        Sheet sheet = workbook.getSheet(new String(string.getBytes(Charset.forName(ws.getEncoding()))));
        if (sheet == null)
        {
            sheet = workbook.getSheet(string);
        }
        return sheet;
    }

    private static void oszlopKonfigurációBeolvasása(Könyvtár könyvtár, Sheet sheet)
    {
        String[][] tábla = new String[sheet.getColumns()][sheet.getRows()];
        for (int aktuálisOszlop = 0; aktuálisOszlop < sheet.getColumns(); aktuálisOszlop++)
        {
            for (int aktuálisSor = 0; aktuálisSor < sheet.getRows(); aktuálisSor++)
            {
                tábla[aktuálisOszlop][aktuálisSor] = sheet.getCell(aktuálisOszlop, aktuálisSor).getContents();
            }
        }
        könyvtár.getKonfiguráció().setTábla(tábla);
    }

    private static void könyvekBeolvasása(Könyvtár könyvtár, Sheet sheet)
    {
        for (int aktuálisOszlop = 0; aktuálisOszlop < sheet.getColumns(); aktuálisOszlop++)
        {
            könyvtár.getOszlopok().add(sheet.getCell(aktuálisOszlop, 0).getContents());
        }

        for (int aktuálisSor = 1; aktuálisSor < sheet.getRows(); aktuálisSor++)
        {
            Könyv könyv = new Könyv(sheet.getColumns() + 1);
            for (int aktuálisOszlop = 0; aktuálisOszlop < sheet.getColumns(); aktuálisOszlop++)
            {
                könyv.setValue(aktuálisOszlop, sheet.getCell(aktuálisOszlop, aktuálisSor).getContents());
            }
            könyvtár.getKönyvek().add(könyv);
        }
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
                e.printStackTrace();
            }
        }

        try
        {
            WritableWorkbook workbook = Workbook.createWorkbook(célFile, getWorkbookSettings());
            WritableSheet writablesheet1 = workbook.createSheet(new String(KÖNYVEK), 0);
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
            WritableSheet writablesheet2 = workbook.createSheet(OSZLOP_KONFIGURÁCIÓ, 1);
            String[][] tábla = könyvtár.getKonfiguráció().getTábla();
            for (int oszlop = 0; oszlop < tábla.length; oszlop++)
            {
                for (int sor = 0; sor < tábla[0].length; sor++)
                {
                    writablesheet2.addCell(new Label(oszlop, sor, tábla[oszlop][sor]));
                }
            }
            // FIXME: konfig mentése
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
        String forrásKönyvtár = FilenameUtils.getFullPath(mentendőFile.getAbsolutePath());
        String forrásFileNév = FilenameUtils.getName(mentendőFile.getAbsolutePath());
        File backupKönyvtár = new File(forrásKönyvtár + "backup");
        backupKönyvtár.mkdirs();
        File backupFile = new File(backupKönyvtár.getAbsoluteFile() + File.separator + forrásFileNév + "_backup_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".zip");
        BackupHelper.zipFile(mentendőFile, backupFile);
    }
}
