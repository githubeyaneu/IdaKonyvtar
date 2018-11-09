package eu.eyan.idakonyvtar.util;

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

import scala.collection.mutable.ListBuffer

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

import eu.eyan.idakonyvtar.model._
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.Library
import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import eu.eyan.log.Log
import eu.eyan.util.backup.BackupHelper
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import jxl.Sheet
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.read.biff.BiffException
import jxl.write.Label
import jxl.write.WritableCellFormat
import eu.eyan.idakonyvtar.util.ExcelHandler.FieldConfiguration
import eu.eyan.idakonyvtar.text.TextsIda
import eu.eyan.idakonyvtar.IdaLibrary
import eu.eyan.util.excel.ExcelPlus
import eu.eyan.util.excel.ExcelColumn
import eu.eyan.util.excel.ExcelCell
import eu.eyan.util.excel.ExcelSheet
import eu.eyan.util.excel.ExcelRow

object ExcelHandler {
  
  @throws(classOf[LibraryException])
  def readLibrary(file: File): Library = {
    try {
      backup(file)
      val libraryWorkbook = Workbook.getWorkbook(file, ExcelPlus.WORKBOOK_SETTINGS)
      val booksExcel = ExcelPlus.sheetToExcel(libraryWorkbook.getSheet(0))
      val colConfig = new FieldConfiguration(ExcelPlus.sheetToExcel(libraryWorkbook.getSheet(1)))
      libraryWorkbook.close

      def createBookField(cell: ExcelCell) = {
        val bookColumn = cell.column
        val bookFieldName = cell.content.get

        val bookFieldTypes: List[BookFieldType] = colConfig.getFieldTypes(bookFieldName)
        val marcCodes = colConfig.getMarcCodes(bookFieldName)
        BookField(bookColumn, bookFieldName, bookFieldTypes, marcCodes)
      }
      val columns = booksExcel.firstRowCells.toList.filter(_.content.getOrElse("").trim.nonEmpty).map(createBookField)
      Log.debug(columns)

      def createBook(excelRow: ExcelRow) = {
        val bs = columns.map(field => (field, booksExcel.getContentOrEmpty((field.excelColumn, excelRow))))
        Book(bs)
      }

      val books = booksExcel.rows.tail.map(createBook)

      new Library(file, columns, books.to[ListBuffer])
    } catch {
      case e: BiffException => throw new LibraryException("Biff Hiba a beolvasásnál " + e.getLocalizedMessage());
      case e: IOException   => throw new LibraryException("IO hiba a beolvasásnál " + e.getLocalizedMessage());
      case t: Throwable     => throw new LibraryException(t.getLocalizedMessage());
    }
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

    val workbook = Workbook.createWorkbook(targetFile, ExcelPlus.WORKBOOK_SETTINGS)
    val booksSheet = workbook.createSheet(EXCEL_SHEET_NAME_BOOKS, 0)
    for { field <- library.getColumns } {
      booksSheet.addCell(new Label(field.excelColumn.index, 0, field.fieldName))
      for (bookIndex <- 0 until library.booksSize) {
        val cellFormat = new WritableCellFormat()
        cellFormat.setWrap(true)
        booksSheet.addCell(new Label(field.excelColumn.index, bookIndex + 1, library.bookAtIndex(bookIndex).getValue(field), cellFormat))
      }
    }
    val columnConfigurationSheet = workbook.createSheet(EXCEL_SHEET_NAME_COLUMN_CONFIGURATION, 1)
    
    
    val texts = IdaLibrary.texts
    
    columnConfigurationSheet.addCell(new Label(COLUMN_FIELD_NAME.index,0,""))
    columnConfigurationSheet.addCell(new Label(COLUMN_MULTIFIELD.index,0,texts.ConfigMultiField.get))
    columnConfigurationSheet.addCell(new Label(COLUMN_AUTOCOMPLETE.index,0,texts.ConfigAutocomplete.get))
    columnConfigurationSheet.addCell(new Label(COLUMN_MARC.index,0,texts.ConfigMarcCode.get))
    columnConfigurationSheet.addCell(new Label(COLUMN_INTABLE.index,0,texts.ConfigInTable.get))
    columnConfigurationSheet.addCell(new Label(COLUMN_REMEMBER.index,0,texts.ConfigRemember.get))
    columnConfigurationSheet.addCell(new Label(COLUMN_PICTURE.index,0,texts.ConfigPicture.get))
    
    for { field <- library.getColumns; row = field.excelColumn.index+1 } {
    	columnConfigurationSheet.addCell(new Label(COLUMN_FIELD_NAME.index,row,field.fieldName))
    	if(field.isMulti) columnConfigurationSheet.addCell(new Label(COLUMN_MULTIFIELD.index,row,texts.ConfigYes.get))
      if(field.isAutocomplete)     	columnConfigurationSheet.addCell(new Label(COLUMN_AUTOCOMPLETE.index,row,texts.ConfigYes.get))
    	columnConfigurationSheet.addCell(new Label(COLUMN_MARC.index,row,field.marcCodes.map(_.toExcel).mkString(",")))
    	if(field.isShowInTable) columnConfigurationSheet.addCell(new Label(COLUMN_INTABLE.index,row,texts.ConfigYes.get))
    	if(field.isRemember) columnConfigurationSheet.addCell(new Label(COLUMN_REMEMBER.index,row,texts.ConfigYes.get))
    	if(field.isPicture) columnConfigurationSheet.addCell(new Label(COLUMN_PICTURE.index,row,texts.ConfigYes.get))
    }

    workbook.write
    workbook.close
    true
  }

  val COLUMN_FIELD_NAME = ExcelColumn(0)
		  val COLUMN_MULTIFIELD = ExcelColumn(1)
		  val COLUMN_AUTOCOMPLETE = ExcelColumn(2)
		  val COLUMN_MARC = ExcelColumn(3)
		  val COLUMN_INTABLE = ExcelColumn(4)
		  val COLUMN_REMEMBER = ExcelColumn(5)
		  val COLUMN_PICTURE = ExcelColumn(6)
		  
  //TODO make private!
	  class FieldConfiguration(val configurationTable: ExcelSheet) {

    case class BookFieldTypeAndColumn(column: ExcelColumn, bookFieldType: BookFieldType)
    val FIELD_TYPES = Map[BookFieldType, ExcelColumn](
      Multifield -> COLUMN_MULTIFIELD, 
      Autocomplete-> COLUMN_AUTOCOMPLETE, 
      InTable -> COLUMN_INTABLE, 
      Remember-> COLUMN_REMEMBER, 
      Picture-> COLUMN_PICTURE, 
          )

    def isMulti(field: BookField) = isConfigSetForField(field, COLUMN_MULTIFIELD)
    def isAutocomplete(field: BookField) = isConfigSetForField(field, COLUMN_AUTOCOMPLETE)
    def isShowInTable(field: BookField) = isConfigSetForField(field, COLUMN_INTABLE)
    def isPicture(field: BookField) = isConfigSetForField(field, COLUMN_PICTURE)
    def isRemember(field: BookField) = isConfigSetForField(field, COLUMN_REMEMBER)
    def getRememberingFields(fields: List[BookField]) = fields.filter(isRemember)

    def getFieldTypes(fieldName: String) = {
      val configRow = configurationTable.rowFromFirstColumn(fieldName)
      if (configRow.nonEmpty) {
        val configCells = FIELD_TYPES.mapValues(column => configurationTable.getCell((column, configRow.get)))
        val yesConfigCells = configCells.filter(_._2.content.map(isTrue).getOrElse(false))
        yesConfigCells.keys.toList
      } else List()
    }

    @throws(classOf[LibraryException])
    def getMarcCodes(fieldName: String) =
      try {
        val marcCodes = getValue(fieldName, COLUMN_MARC).getOrElse(EMPTY_STRING).split(MARC_CODES_SEPARATOR)
        for { marcCode <- marcCodes; codes = marcCode.split(MARC_CODE_SEPARATOR) if codes.length > 2 } yield new Marc(codes(0), codes(1), codes(2), null)
      } catch {
        case e: Exception =>
          Log.error(e)
          throw new LibraryException("A Marc kódot nem lehet a configurationból beolvasni. fieldName=" + fieldName, e)
      }

    private def isConfigSetForField(fieldName: String, column: ExcelColumn): Boolean = getValue(fieldName, column).map(isTrue).getOrElse(false)
    private def isConfigSetForField(field: BookField, column: ExcelColumn): Boolean = getValue(field, column).map(isTrue).getOrElse(false)

    private def getValue(fieldName: String, column: ExcelColumn) = {
      val rowOpt = configurationTable.rowFromFirstColumn(fieldName)
      val colRow = rowOpt.map((column, _))
      colRow.map(configurationTable.getCell).map(_.content).flatten
    }
    private def getValue(field: BookField, column: ExcelColumn) = {
      val rowOpt = configurationTable.rowFromFirstColumn(field.fieldName)
      val colRow = rowOpt.map((column, _))
      colRow.map(configurationTable.getCell).map(_.content).flatten
    }

    private def isTrue(string: String) = string.trim.nonEmpty
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