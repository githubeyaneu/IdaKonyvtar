package eu.eyan.idakonyvtar.model;

import scala.annotation.varargs

import eu.eyan.idakonyvtar.model.ColumnConfigurations.ColumnConfigurationsValue
import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.idakonyvtar.util.LibraryException

object ColumnKonfiguration {
  class Builder(columns: Int, rows: Int) {
    val columnConfiguration = new ColumnKonfiguration()
    var actualRow: Int = 0
    columnConfiguration.setTable(Array.ofDim[String](columns, rows))

    @varargs def withRow(values: String*): Builder = {
      for (i <- 0 until values.length) this.columnConfiguration.getTable()(i)(actualRow) = values(i)
      actualRow = actualRow + 1
      this
    }

    def build() = columnConfiguration
  }
}

//FIXME: Refactor, because it cannot be understood, constant pain i t a
class ColumnKonfiguration {

  type ColumnKonfigurationTable = Array[Array[String]]
  var table: ColumnKonfigurationTable = null

  def isTrue(columnName: String, columnConfiguration: ColumnConfigurationsValue) =
    getValue(columnName, columnConfiguration).equalsIgnoreCase("Igen")

  def getValue(columnName: String, columnConfiguration: ColumnConfigurationsValue): String = {
    val columnIndex = getColumnIndex(columnName)
    val configurationIndex = getConfigurationIndex(columnConfiguration)

    if (columnIndex > 0 && configurationIndex > 0) table(configurationIndex)(columnIndex)
    else ""
  }

  def getColumnIndex(columnName: String): Int = table(0).map(_.toLowerCase).indexOf(columnName.toLowerCase)

  def getConfigurationIndex(configurationName: ColumnConfigurationsValue): Int = {
    val t = table.transpose
    t(0).map(_.toLowerCase).indexOf(configurationName.name.toLowerCase)
  }

  @throws(classOf[LibraryException])
  def getMarcCodes(columnName: String) = {
    try {
      val marcCodeTexts = getValue(columnName, ColumnConfigurations.MARC_CODE).split(",")
      for (string <- marcCodeTexts if string.split("-").length > 2) yield {
        val codes = string.split("-")
        new Marc(codes(0), codes(1), codes(2), null)
      }
    } catch {
      case e: Exception =>
        throw new LibraryException("A Marc kódot nem lehet a configurationból beolvasni: " + columnName)
    }
  }

  def getRememberingColumns() =
    for (columnIndex <- 1 until table(0).length if isTrue(table(0)(columnIndex), ColumnConfigurations.REMEMBERING)) yield table(0)(columnIndex)

  def getTable() = table

  def setTable(table: ColumnKonfigurationTable) = this.table = table

}
