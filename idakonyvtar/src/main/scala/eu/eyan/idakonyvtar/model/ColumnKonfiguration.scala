package eu.eyan.idakonyvtar.model;

import scala.annotation.varargs

import eu.eyan.idakonyvtar.model.ColumnConfigurations.ColumnConfigurationsValue
import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.idakonyvtar.util.LibraryException

object ColumnKonfiguration {
  class Builder(columns: Int, rows: Int) {
    val columnConfiguration = new ColumnKonfiguration(Array.ofDim[String](columns, rows))
    var actualRow: Int = 0
    // columnConfiguration.setTable()

    @varargs def withRow(values: String*): Builder = {
      for {i <- 0 until values.length} this.columnConfiguration.getTable()(i)(actualRow) = values(i)
      actualRow = actualRow + 1
      this
    }

    def build() = columnConfiguration
  }
}

// FIXME: Refactor, because it cannot be understood, constant pain i t a
class ColumnKonfiguration(val table: Array[Array[String]]) {

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
      for {string <- marcCodeTexts if string.split("-").length > 2} yield {
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
  }

  def getRememberingColumns() =
    for {columnIndex <- 1 until table(0).length if isTrue(table(0)(columnIndex), ColumnConfigurations.REMEMBERING)} yield table(0)(columnIndex)

  def getTable() = table
<<<<<<< HEAD

  // def setTable(table: ColumnKonfigurationTable) = this.table = table

=======
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
}