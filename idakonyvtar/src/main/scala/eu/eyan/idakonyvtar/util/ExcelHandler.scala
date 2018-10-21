package eu.eyan.idakonyvtar.util;

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.FieldConfiguration
import eu.eyan.idakonyvtar.model.Library
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import eu.eyan.log.Log
import eu.eyan.util.backup.BackupHelper
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.read.biff.BiffException
import jxl.write.Label
import jxl.write.WritableCellFormat
import jxl.write.WriteException
import jxl.write.biff.RowsExceededException
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import jxl.Sheet

object ExcelHandler {

  def readExcelFromFile(file: File, sheetName: String) = {
    Log.info(s"$file exists: ${file.exists}")
    val workbook = Workbook.getWorkbook(file, WORKBOOK_SETTINGS)
    val excel = workbookSheetToExcel(workbook, sheetName)
    workbook.close
    excel
  }

  def readExcelFromStream(data: InputStream, sheetName: String) = {
    Log.info
    val workbook = Workbook.getWorkbook(data, WORKBOOK_SETTINGS)
    val excel = workbookSheetToExcel(workbook, sheetName)
    workbook.close
    excel
  }

  def WORKBOOK_SETTINGS = { val ws = new WorkbookSettings(); ws.setEncoding("Cp1252"); ws }

  case class Column(index: Int)
  case class Row(index: Int)
  case class Cell(column: Column, row: Row, content: Option[String])

  case class Excel(val columns: IndexedSeq[Column], val rows: IndexedSeq[Row], private val cells: Map[(Column, Row), String]) {

    def firstRowCells = rowCells(Row(0))
    def firstColumnCells = columnCells(Column(0))

    def rowFromFirstColumn(contentToSearchInFirstColumn: String) = firstColumnCells.filter(_.content == Option(contentToSearchInFirstColumn)).map(_.row).lift(0)
    def columnFromFirstRow(contentToSearchInFirstRow: String) = firstRowCells.filter(_.content == Option(contentToSearchInFirstRow)).map(_.column).lift(0)

    def getCell(columnRow: (Column, Row)) = Cell(columnRow._1, columnRow._2, cells.get(columnRow))
        // FIXME refactor to this : cells.get(columnRow).map(value => Cell(columnRow._1, columnRow._2, value))
    // FIXME: refactor to this: case class Cell(column: Column, row: Row, content: String)

    private def columnCells(column: Column) = rows.map(row => getCell((column, row)))
    private def rowCells(row: Row) = columns.map(column => getCell((column, row)))
  }

  private def workbookSheetToExcel(workbook: Workbook, sheetName: String) = {
    Log.info(s"Sheets: ${workbook.getSheetNames.mkString}")
    val sheet = getSheet(WORKBOOK_SETTINGS, workbook, sheetName)
    sheetToExcel(sheet)
  }

  private def sheetToExcel(sheet: Sheet) = {
		  val columns = for (columnIndex <- 0 until sheet.getColumns) yield Column(columnIndex)
		  val rows = for (rowIndex <- 0 until sheet.getRows) yield Row(rowIndex)
		  val table = for (column <- columns; row <- rows) yield ((column, row), sheet.getCell(column.index, row.index).getContents)
		  Excel(columns, rows, table.toMap)
  }

  @throws(classOf[LibraryException])
  def readLibrary(file: File): Library = {
    try {
      backup(file)
      val libraryWorkbook = Workbook.getWorkbook(file, WORKBOOK_SETTINGS)
      
//      val columnConfigSheet = libraryWorkbook.getSheet(1)
//      val configTable = (
//        for { col <- 0 until columnConfigSheet.getColumns() }
//          yield (
//          for { row <- 0 until columnConfigSheet.getRows() }
//            yield columnConfigSheet.getCell(col, row).getContents()).toArray).toArray
//      Log.debug(configTable.map(_.mkString(", ")).mkString("\r\n"))

      sheetToExcel(libraryWorkbook.getSheet(1))
      
      val colConfig = new FieldConfiguration(sheetToExcel(libraryWorkbook.getSheet(1)))

      
      val booksSheet = libraryWorkbook.getSheet(0)
      val columns = (for { actualColumn <- 0 until booksSheet.getColumns() } yield booksSheet.getCell(actualColumn, 0).getContents()).toList.filter(_.nonEmpty)
      val books = for { actualRow <- 1 until booksSheet.getRows() } yield {
        val book = Book(booksSheet.getColumns())
        for (actualColumn <- 0 until booksSheet.getColumns()) {
          val contents = booksSheet.getCell(actualColumn, actualRow).getContents()
          book.setValue(actualColumn)(contents)
        }
        book
      }

      val library = new Library(file, colConfig, columns)
      for { book <- books } library.addBook(book)
      libraryWorkbook.close

      library
    } catch {
      case e: BiffException => throw new LibraryException("Biff Hiba a beolvasásnál " + e.getLocalizedMessage());
      case e: IOException   => throw new LibraryException("IO hiba a beolvasásnál " + e.getLocalizedMessage());
      case t: Throwable     => throw new LibraryException(t.getLocalizedMessage());
    }
  }

  def getSheet(ws: WorkbookSettings, workbook: Workbook, string: String) = {
    val sheet = workbook.getSheet(new String(string.getBytes(Charset.forName(ws.getEncoding()))))
    if (sheet == null) workbook.getSheet(string)
    else sheet
  }

  @throws(classOf[Exception])
  def saveLibrary(targetFile: File, library: Library): Boolean = {
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

    val workbook = Workbook.createWorkbook(targetFile, WORKBOOK_SETTINGS)
    val booksSheet = workbook.createSheet(EXCEL_SHEET_NAME_BOOKS, 0)
    for { columnIndex <- 0 until library.columns.size } {
      booksSheet.addCell(new Label(columnIndex, 0, library.columns(columnIndex)))
      for (bookIndex <- 0 until library.booksSize) {
        val cellFormat = new WritableCellFormat()
        cellFormat.setWrap(true)
        booksSheet.addCell(new Label(columnIndex, bookIndex + 1, library.bookAtIndex(bookIndex).getValue(columnIndex), cellFormat))
      }
    }
    val columnConfigurationSheet = workbook.createSheet(EXCEL_SHEET_NAME_COLUMN_CONFIGURATION, 1)
    val table = library.configuration.configurationTable
    
    for { column <- table.columns; row <- table.rows } {
      val cell = table.getCell((column, row))
      cell.content.foreach{ content =>
        columnConfigurationSheet.addCell(new Label(cell.column.index, cell.row.index, content))
      }
    	
    }

    workbook.write
    workbook.close
    true
  }

  @throws(classOf[Exception])
  private def backup(fileToSave: File) = {
    val sourceLibrary = FilenameUtils.getFullPath(fileToSave.getAbsolutePath)
    val sourceFileName = FilenameUtils.getName(fileToSave.getAbsolutePath)
    val backupDir = new File(sourceLibrary + "backup").mkDirs.getAbsoluteFile.toString
    val backupFile = new File(BACKUP_FILE_TEMPLATE(backupDir, sourceFileName))
    BackupHelper.zipFile(fileToSave, backupFile)
  }
}