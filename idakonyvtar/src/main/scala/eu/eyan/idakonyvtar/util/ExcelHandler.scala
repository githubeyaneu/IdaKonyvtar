package eu.eyan.idakonyvtar.util;

import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

import eu.eyan.idakonyvtar.IdaLibrary
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.ColumnKonfiguration
import eu.eyan.idakonyvtar.model.Library
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.read.biff.BiffException
import jxl.write.Label
import jxl.write.WritableCellFormat
import jxl.write.WriteException
import jxl.write.biff.RowsExceededException
import eu.eyan.util.backup.BackupHelper
import eu.eyan.log.Log
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.idakonyvtar.text.TechnicalTextsIda
import java.io.InputStream

object ExcelHandler {

  val COLUMN_CONFIGURATION = "OszlopKonfiguráció"
  val BOOKS = "Könyvek"
  val ERROR_TEXT = "Nem sikerült a mentés."

  case class Excel(val columns: Int, val rows: Int, val cells: Map[(Int, Int), String]){
    def row(rowIndex: Int) = for(columnIndex <- 0 until columns) yield cells((columnIndex, rowIndex)) 
		def column(columnIndex: Int) = for(rowIndex <- 0 until rows) yield cells((columnIndex, rowIndex))
    def rowIndex(firstRowContent: String) = column(0).zipWithIndex.filter(_._1 == firstRowContent).map(_._2).lift(0)
    def columnIndex(firstColumnContent: String) = row(0).zipWithIndex.filter(_._1 == firstColumnContent).map(_._2).lift(0)
  }
  
  def readExcel(file: File, sheetName: String) = {
    Log.info(s"$file exists: ${file.exists}")
    val workbook = Workbook.getWorkbook(file, getWorkbookSettings())

    Log.info(s"Sheets: ${workbook.getSheetNames.mkString}")
    
    val sheet = getSheet(getWorkbookSettings(), workbook, sheetName)

    val columnCount = sheet.getColumns
    val rowCount = sheet.getRows
    val table = 
      for ( columnIndex <- 0 until columnCount; rowIndex <- 0 until rowCount ) 
        yield ((columnIndex, rowIndex),sheet.getCell(columnIndex, rowIndex).getContents)
    workbook.close
    Excel(columnCount, rowCount, table.toMap)
  }

  def readExcelStream(data: InputStream, sheetName: String) = {
		  Log.info
		  val workbook = Workbook.getWorkbook(data, getWorkbookSettings())
		  
		  Log.info(s"Sheets: ${workbook.getSheetNames.mkString}")
		  
		  val sheet = getSheet(getWorkbookSettings(), workbook, sheetName)
		  
		  val columnCount = sheet.getColumns
		  val rowCount = sheet.getRows
		  val table = 
		  for ( columnIndex <- 0 until columnCount; rowIndex <- 0 until rowCount ) 
			  yield ((columnIndex, rowIndex),sheet.getCell(columnIndex, rowIndex).getContents)
			  workbook.close
			  Excel(columnCount, rowCount, table.toMap)
  }

  @throws(classOf[LibraryException])
  def readLibrary(file: File): Library = {
    try {
      backup(file)
      val libraryWorkbook = Workbook.getWorkbook(file, getWorkbookSettings())
      val booksSheet = getSheet(getWorkbookSettings(), libraryWorkbook, BOOKS)
      val columnConfigSheet = getSheet(getWorkbookSettings(), libraryWorkbook, COLUMN_CONFIGURATION)

      val configTable = (
        for { col <- 0 until columnConfigSheet.getColumns() }
          yield (
          for { row <- 0 until columnConfigSheet.getRows() }
            yield columnConfigSheet.getCell(col, row).getContents()).toArray).toArray

      Log.debug(configTable.map(_.mkString(", ")).mkString("\r\n"))

      val colConfig = new ColumnKonfiguration(configTable)

      val columns = for { actualColumn <- 0 until booksSheet.getColumns() } yield booksSheet.getCell(actualColumn, 0).getContents()

      val books = for { actualRow <- 1 until booksSheet.getRows() } yield {
        val book = Book(booksSheet.getColumns())
        for (actualColumn <- 0 until booksSheet.getColumns()) {
          val contents = booksSheet.getCell(actualColumn, actualRow).getContents()
          book.setValue(actualColumn, contents)
        }
        book
      }

      val library = new Library(colConfig, columns)
      for { book <- books } library.books.add(book)
      libraryWorkbook.close
      
      library
    } catch {
      case e: BiffException => throw new LibraryException("Biff Hiba a beolvasásnál " + e.getLocalizedMessage());
      case e: IOException   => throw new LibraryException("Hiba a beolvasásnál " + e.getLocalizedMessage());
      case e: Exception     => throw new LibraryException(e.getLocalizedMessage());
    }
  }

  def getWorkbookSettings(): WorkbookSettings = {
    val ws = new WorkbookSettings()
    ws.setEncoding("Cp1252")
    ws
  }

  def getSheet(ws: WorkbookSettings, workbook: Workbook, string: String) = {
    val sheet = workbook.getSheet(new String(string.getBytes(Charset.forName(ws.getEncoding()))))
    if (sheet == null) workbook.getSheet(string)
    else sheet
  }

  @throws(classOf[LibraryException])
  def saveLibrary(targetFile: File, library: Library) = {
    if (targetFile.exists())
      if (targetFile.isFile())
        try {
          backup(targetFile);
          FileUtils.forceDelete(targetFile)
        } catch {
          case e: IOException => e.printStackTrace()
        }
      else
        throw new LibraryException("A választott cél nem file: " + targetFile)

    try {
      val workbook = Workbook.createWorkbook(targetFile, getWorkbookSettings())
      val booksSheet = workbook.createSheet(BOOKS, 0)
      for { columnIndex <- 0 until library.columns.size } {
        booksSheet.addCell(new Label(columnIndex, 0, library.columns(columnIndex)))
        for (bookIndex <- 0 until library.books.size()) {
          val cellFormat = new WritableCellFormat()
          cellFormat.setWrap(true)
          booksSheet.addCell(new Label(columnIndex, bookIndex + 1, library.books.get(bookIndex).getValue(columnIndex), cellFormat))
        }
      }
      val columnConfigurationSheet = workbook.createSheet(COLUMN_CONFIGURATION, 1)
      val table = library.configuration.getTable()
      for { column <- 0 until table.length; sor <- 0 until table(0).length }
        columnConfigurationSheet.addCell(new Label(column, sor, table(column)(sor)))

      workbook.write
      workbook.close
    } catch {
      case e: IOException           => throw new LibraryException(ERROR_TEXT, e)
      case e: RowsExceededException => throw new LibraryException(ERROR_TEXT, e)
      case e: WriteException        => throw new LibraryException(ERROR_TEXT, e)
    }
  }

  @throws(classOf[LibraryException])
  private def backup(fileToSave: File) = {
    val sourceLibrary = FilenameUtils.getFullPath(fileToSave.getAbsolutePath())
    val sourceFileName = FilenameUtils.getName(fileToSave.getAbsolutePath())
    val backupLibrary = new File(sourceLibrary + "backup")
    backupLibrary.mkdirs()
    val backupFile = new File(
      backupLibrary.getAbsoluteFile()
        + File.separator
        + sourceFileName
        + "_backup_"
        + "v" + TechnicalTextsIda.VERSION + "_"
        + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        .format(new Date()) + ".zip");
    BackupHelper.zipFile(fileToSave, backupFile);
  }
}