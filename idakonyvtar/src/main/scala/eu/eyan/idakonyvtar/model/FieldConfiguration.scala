package eu.eyan.idakonyvtar.model;

import scala.annotation.varargs

import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.idakonyvtar.util.LibraryException
import eu.eyan.idakonyvtar.util.ExcelHandler.Excel
import eu.eyan.idakonyvtar.util.ExcelHandler.Column
import eu.eyan.idakonyvtar.util.ExcelHandler.Row
import scala.collection.mutable.Map
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.log.Log

class FieldConfiguration(val configurationTable: Excel) {
  val YESS = List(IGEN, YES, JA)
  val COLUMN_MULTIFIELD = Column(1)
  val COLUMN_AUTOCOMPLETE = Column(2)
  val COLUMN_MARC = Column(3)
  val COLUMN_INTABLE = Column(4)
  val COLUMN_REMEMBER = Column(5)
  val COLUMN_PICTURE = Column(6)

  // TODO: use Row s instead of strings -> bigger refact
  def isMulti(fieldName: String) = isConfigSetForField(fieldName, COLUMN_MULTIFIELD)
  def isAutocomplete(fieldName: String) = isConfigSetForField(fieldName, COLUMN_AUTOCOMPLETE)
  def isShowInTable(fieldName: String) = isConfigSetForField(fieldName, COLUMN_INTABLE)
  def isPicture(fieldName: String) = isConfigSetForField(fieldName, COLUMN_PICTURE)
  def isRemember(fieldName: String) = isConfigSetForField(fieldName, COLUMN_REMEMBER)
  def getRememberingColumns() = for { field <- configurationTable.firstColumnCells if isRemember(field.content.getOrElse("")) } yield field.content.get

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

  private def isConfigSetForField(fieldName: String, column: Column): Boolean = getValue(fieldName, column).map(isTrue).getOrElse(false)

  private def getValue(fieldName: String, column: Column) = {
    val rowOpt = configurationTable.rowFromFirstColumn(fieldName)
    val colRow = rowOpt.map((column, _))
    colRow.map(configurationTable.getCell).map(_.content).flatten
  }

  private def isTrue(string: String) = string.containsAnyIgnoreCase(YESS)
}