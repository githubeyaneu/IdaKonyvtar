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

import eu.eyan.idakonyvtar.model.Book;
import eu.eyan.idakonyvtar.model.Library;

object ExcelHandler {

  val COLUMN_CONFIGURATION = "OszlopKonfiguráció"
  val BOOKS = "Könyvek"

  @throws(classOf[LibraryException])
  def readLibrary(file: File): Library = {
    backup(file);
    val library = new Library();
    try {
      val workbook = Workbook.getWorkbook(file, getWorkbookSettings())
      readBooks(library, getSheet(getWorkbookSettings(), workbook, BOOKS))
      readColumnConfiguration(library, getSheet(getWorkbookSettings(), workbook, COLUMN_CONFIGURATION))
    } catch {
      case e: BiffException => {
        e.printStackTrace()
        JOptionPane.showMessageDialog(null,
          "Biff Hiba a beolvasásnál " + e.getLocalizedMessage());
      }
      case e: IOException => {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null,
          "Hiba a beolvasásnál " + e.getLocalizedMessage());
      }
      case e: Exception => {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
      }
    }
    return library
  }

  def getWorkbookSettings(): WorkbookSettings = {
    val ws = new WorkbookSettings()
    ws.setEncoding("Cp1252")
    ws
  }

  def getSheet(ws: WorkbookSettings, workbook: Workbook, string: String) = {
    var sheet = workbook.getSheet(new String(string.getBytes(Charset.forName(ws.getEncoding()))))
    if (sheet == null) sheet = workbook.getSheet(string)
    sheet
  }

  def readColumnConfiguration(library: Library, sheet: Sheet) = {
    val table = Array.ofDim[String](sheet.getColumns(), sheet.getRows())
    for (actualColumn <- 0 until sheet.getColumns(); actualRow <- 0 until sheet.getRows())
      table(actualColumn)(actualRow) = sheet.getCell(actualColumn, actualRow).getContents()
    library.configuration.setTable(table)
  }

  def readBooks(library: Library, sheet: Sheet) = {
    for (actualColumn <- 0 until sheet.getColumns())
      library.columns += sheet.getCell(actualColumn, 0).getContents()

    for (actualRow <- 1 until sheet.getRows()) {
      val book = Book.apply(sheet.getColumns() + 1)
      // boolean isEmpty = true;
      for (actualColumn <- 0 until sheet.getColumns()) {
        val contents = sheet.getCell(actualColumn, actualRow).getContents()
        // if (contents != null && contents.length() != 0)
        // {
        // isEmpty = false;
        // System.out.println("-" + contents + "-");
        // }
        book.setValue(actualColumn, contents)
      }
      library.books.add(book);
    }
  }

  @throws(classOf[LibraryException])
  def saveLibrary(targetFile: File, library: Library) = {
    if (targetFile.exists() && !targetFile.isFile()) {
      throw new LibraryException("Nem file: " + targetFile)
    }

    if (targetFile.exists()) {
      backup(targetFile);
      try {
        FileUtils.forceDelete(targetFile)
      } catch {
        case e: IOException => e.printStackTrace()
      }
    }

    try {
      val workbook = Workbook.createWorkbook(targetFile, getWorkbookSettings())
      val writablesheet1 = workbook.createSheet(BOOKS, 0)
      for (columnIndex <- 0 until library.columns.size) {
        writablesheet1.addCell(new Label(columnIndex, 0, library.columns(columnIndex)))
        for (bookIndex <- 0 until library.books.size()) {
          val cellFormat = new WritableCellFormat()
          cellFormat.setWrap(true)
          writablesheet1.addCell(new Label(columnIndex, bookIndex + 1, library.books.get(bookIndex).getValue(columnIndex), cellFormat))
        }
      }
      val writablesheet2 = workbook.createSheet(COLUMN_CONFIGURATION, 1)
      val table = library.configuration.getTable()
      for (column <- 0 until table.length; sor <- 0 until table(0).length)
        writablesheet2.addCell(new Label(column, sor, table(column)(sor)))

      // FIXME: save konfiguration... question: is it possible to change
      // any konfiguration from the application?
      workbook.write();
      workbook.close();
    } catch {
      case e: IOException           => e.printStackTrace()
      case e: RowsExceededException => e.printStackTrace()
      case e: WriteException        => e.printStackTrace()
    }
  }

  @throws(classOf[LibraryException])
  private def backup(fileToSave: File) = {
    val sourceLibrary = FilenameUtils.getFullPath(fileToSave.getAbsolutePath())
    val sourceFileName = FilenameUtils.getName(fileToSave.getAbsolutePath())
    val backupLibrary = new File(sourceLibrary + "backup")
    // if (
    backupLibrary.mkdirs()
    val backupFile = new File(
      backupLibrary.getAbsoluteFile()
        + File.separator
        + sourceFileName
        + "_backup_"
        + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        .format(new Date()) + ".zip");
    BackupHelper.zipFile(fileToSave, backupFile);
    // } else {
    // throw new LibraryException(
    // "Nem sikerült a backup könyvtárt létrehozni: "
    // + backupLibrary);
    // }
  }
}
