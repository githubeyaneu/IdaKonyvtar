package eu.eyan.idakonyvtar.model;

import scala.annotation.varargs

import eu.eyan.idakonyvtar.model.ColumnConfigurations.ColumnConfigurationsValue
import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.idakonyvtar.util.LibraryException
import eu.eyan.idakonyvtar.util.ExcelHandler.Excel
import eu.eyan.idakonyvtar.util.ExcelHandler.Column
import eu.eyan.idakonyvtar.util.ExcelHandler.Row
import scala.collection.mutable.Map
import eu.eyan.idakonyvtar.text.TechnicalTextsIda

object FieldConfiguration {
  //TODO onlz for test remove from here
  class Builder(columnCount: Int, rowCount: Int) {
    @varargs def withRow(values: String*): Builder = {
      for { columnIndex <- 0 until values.length } table.put((Column(columnIndex), Row(actualRow)), values(columnIndex))
      actualRow = actualRow + 1
      this
    }

    private val columns = for (columnIndex <- 0 until columnCount) yield Column(columnIndex)
    private val rows = for (rowIndex <- 0 until rowCount) yield Row(rowIndex)
    private val table = Map[(Column, Row), String]()
    private var actualRow: Int = 0

    def build() = new FieldConfiguration(new Excel(columns, rows, table.toMap))
  }
}

//TODO delete it! delete them all!
object ColumnConfigurations extends Enumeration {

  type ColumnConfigurations = ColumnConfigurationsValue

  case class ColumnConfigurationsValue(name: String) extends Val(name)

  val AUTOCOMPLETE = columnConfigurationsInternalValue("AutoComplete")
  val MARC_CODE = columnConfigurationsInternalValue("MarcKód")
  val REMEMBERING = columnConfigurationsInternalValue("Emlékező")
  val SHOW_IN_TABLE = columnConfigurationsInternalValue("Táblázatban")
  val PICTURE = columnConfigurationsInternalValue("Kép")

  protected final def columnConfigurationsInternalValue(name: String): ColumnConfigurationsValue = ColumnConfigurationsValue(name)
}

// FIXME: Refactor, because it cannot be understood, constant pain i t a
class FieldConfiguration(val configurationTable: Excel) {
  def isMulti(fieldName: String) = isTrue(fieldName, TechnicalTextsIda.CONFIG_NAME_MULTIFIELD)


  def isTrue(fieldName: String, configName: String) = getValue(fieldName, configName).equalsIgnoreCase("Igen")
  private def getValue(fieldName: String, configName: String) = {
    val fieldIndex = getColumnIndex(fieldName)
    val configurationIndex = getConfigurationIndex(configName)

    if (fieldIndex.index > 0 && configurationIndex.index > 0) configurationTable.getCell((configurationIndex, fieldIndex)).content.get
    else ""
  }
  private def getConfigurationIndex(configName: String) =
    configurationTable.firstRowCells.filter(_.content.get.toLowerCase == configName.toLowerCase)(0).column

    //TODO remove and make only relevant methods
    def isTrue(fieldName: String, columnConfiguration: ColumnConfigurationsValue) = getValue(fieldName, columnConfiguration).equalsIgnoreCase("Igen")
  def getRememberingColumns() = List[String]() //FIXME for { columnIndex <- 1 until table(0).length if isTrue(table(0)(columnIndex), ColumnConfigurations.REMEMBERING) } yield table(0)(columnIndex)

  //  @throws(classOf[LibraryException])
  def getMarcCodes(columnName: String) = //List(new Marc("","","", null))
    try {
      val marcCodeTexts = getValue(columnName, ColumnConfigurations.MARC_CODE).split(",")
      for { string <- marcCodeTexts if string.split("-").length > 2 } yield {
        val codes = string.split("-")
        val MARC_CODE_0 = 0
        val MARC_CODE_1 = 1
        val MARC_CODE_2 = 2
        new Marc(codes(MARC_CODE_0), codes(MARC_CODE_1), codes(MARC_CODE_2), null)
      }
    } catch {
      case e: Exception =>
        throw new LibraryException("A Marc kódot nem lehet a configurationból beolvasni: " + columnName)
    }

  private def getValue(fieldName: String, columnConfiguration: ColumnConfigurationsValue): String = {
    val columnIndex = getColumnIndex(fieldName)
    val configurationIndex = getConfigurationIndex(columnConfiguration)

    if (columnIndex.index > 0 && configurationIndex.index > 0) configurationTable.getCell((configurationIndex, columnIndex)).content.get
    else ""
  }

  private def getColumnIndex(fieldName: String) = configurationTable.rowFromFirstColumn(fieldName).getOrElse(Row(-1))

  private def getConfigurationIndex(configurationName: ColumnConfigurationsValue) =
    configurationTable.firstRowCells.filter(_.content.get.toLowerCase == configurationName.name.toLowerCase)(0).column

}





//object ColumnKonfiguration {
//  class Builder(columns: Int, rows: Int) {
//    val columnConfiguration = new ColumnKonfiguration(Array.ofDim[String](columns, rows))
//    var actualRow: Int = 0
//    // columnConfiguration.setTable()
//
//    @varargs def withRow(values: String*): Builder = {
//      for {i <- 0 until values.length} this.columnConfiguration.getTable()(i)(actualRow) = values(i)
//      actualRow = actualRow + 1
//      this
//    }
//
//    def build() = columnConfiguration
//  }
//}
//

//// FIXME: Refactor, because it cannot be understood, constant pain i t a
//class ColumnKonfiguration(val table: Array[Array[String]]) {
//
//  def isTrue(columnName: String, columnConfiguration: ColumnConfigurationsValue) =
//    getValue(columnName, columnConfiguration).equalsIgnoreCase("Igen")
//
//  def getValue(columnName: String, columnConfiguration: ColumnConfigurationsValue): String = {
//    val columnIndex = getColumnIndex(columnName)
//    val configurationIndex = getConfigurationIndex(columnConfiguration)
//
//    if (columnIndex > 0 && configurationIndex > 0) table(configurationIndex)(columnIndex)
//    else ""
//  }
//
//  def getColumnIndex(columnName: String): Int = table(0).map(_.toLowerCase).indexOf(columnName.toLowerCase)
//
//  def getConfigurationIndex(configurationName: ColumnConfigurationsValue): Int = {
//    val t = table.transpose
//    t(0).map(_.toLowerCase).indexOf(configurationName.name.toLowerCase)
//  }
//
//  @throws(classOf[LibraryException])
//  def getMarcCodes(columnName: String) = {
//    try {
//      val marcCodeTexts = getValue(columnName, ColumnConfigurations.MARC_CODE).split(",")
//      for {string <- marcCodeTexts if string.split("-").length > 2} yield {
//        val codes = string.split("-")
//        val MARC_CODE_0 = 0
//        val MARC_CODE_1 = 1
//        val MARC_CODE_2 = 2
//        new Marc(codes(MARC_CODE_0), codes(MARC_CODE_1), codes(MARC_CODE_2), null)
//      }
//    } catch {
//      case e: Exception =>
//        throw new LibraryException("A Marc kódot nem lehet a configurationból beolvasni: " + columnName)
//    }
//  }
//
//  def getRememberingColumns() =
//    for {columnIndex <- 1 until table(0).length if isTrue(table(0)(columnIndex), ColumnConfigurations.REMEMBERING)} yield table(0)(columnIndex)
//
//  def getTable() = table
//}