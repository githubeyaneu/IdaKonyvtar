package eu.eyan.idakonyvtar.model;

import com.google.common.collect.Lists.newArrayList

import java.util.ArrayList
import java.util.List

import eu.eyan.idakonyvtar.oszk.Marc

object ColumnKonfiguration {
  //  object ColumnConfigurations extends Enumeration {
  //
  //    type ColumnConfigurations = ColumnConfigurationsValue
  //
  //    case class ColumnConfigurationsValue(name: String) extends Val(name) {
  //      def getName() = name
  //    }
  //
  //    //  val CANCEL_BUTTON = ControlTextInternalValue("Cancel", "Cancel the changes and dismiss the dialog")
  //    //  val OK_BUTTON = ControlTextInternalValue("OK", "Save the changes and dismiss the dialog")
  //    val MULTIFIELD = ColumnConfigurationsInternalValue("MultiMező")
  //    val AUTOCOMPLETE = ColumnConfigurationsInternalValue("AutoComplete")
  //    val MARC_CODE = ColumnConfigurationsInternalValue("MarcKód")
  //    val REMEMBERING = ColumnConfigurationsInternalValue("Emlékező")
  //    val SHOW_IN_TABLE = ColumnConfigurationsInternalValue("Táblázatban")
  //
  //    protected final def ColumnConfigurationsInternalValue(name: String): ColumnConfigurationsValue =
  //      ColumnConfigurationsValue(name)
  //
  //  }

  class Builder(columns: Int, rows: Int) {
    val columnConfiguration = new ColumnKonfiguration()
    var actualRow: Int = 0
    columnConfiguration.setTable(Array.ofDim[String](columns, rows))

    // FIXME phüjj...
    //    def withRow(values: String*): Builder = {
    //      for (i <- 0 until values.length) this.columnConfiguration.getTable()(i)(actualRow) = values(i)
    //      actualRow = actualRow + 1
    //      this
    //    }

    def withRow(value1: String): Builder = {
      val values = Array(value1)
      for (i <- 0 until values.length) this.columnConfiguration.getTable()(i)(actualRow) = values(i)
      actualRow = actualRow + 1
      this
    }
    def withRow(value1: String, value2: String): Builder = {
      val values = Array(value1, value2)
      for (i <- 0 until values.length) this.columnConfiguration.getTable()(i)(actualRow) = values(i)
      actualRow = actualRow + 1
      this
    }
    def withRow(value1: String, value2: String, value3: String): Builder = {
      val values = Array(value1, value2, value3)
      for (i <- 0 until values.length) this.columnConfiguration.getTable()(i)(actualRow) = values(i)
      actualRow = actualRow + 1
      this
    }
    def withRow(value1: String, value2: String, value3: String, value4: String): Builder = {
      val values = Array(value1, value2, value3, value4)
      for (i <- 0 until values.length) this.columnConfiguration.getTable()(i)(actualRow) = values(i)
      actualRow = actualRow + 1
      this
    }

    def build() = columnConfiguration
  }
}

//FIXME: Refactor, because it cannot be understood, constant pain i t a
class ColumnKonfiguration {

  var table: Array[Array[String]] = null

  def isTrue(columnName: String, columnConfiguration: ColumnConfigurations) =
    getValue(columnName, columnConfiguration).equalsIgnoreCase("Igen")

  def getValue(columnName: String,
               columnConfiguration: ColumnConfigurations): String = {
    val columnIndex = getColumnIndex(columnName)
    val configurationIndex = getConfigurationIndex(columnConfiguration)

    if (columnIndex > 0 && configurationIndex > 0) table(configurationIndex)(columnIndex) else ""
  }

  def getColumnIndex(columnName: String): Int = {
    if (table.length > 0) {
      for (configurationIndex <- 0 until table(0).length) {
        if ((table(0)(configurationIndex)).equalsIgnoreCase(columnName)) {
          return configurationIndex
        }
      }
    }
    -1
  }

  def getConfigurationIndex(configurationName: ColumnConfigurations): Int = {
    for (columnIndex <- 0 until table.length) {
      if (table(columnIndex)(0).equalsIgnoreCase(configurationName.getName())) {
        return columnIndex
      }
    }
    -1
  }

  def getMarcCodes(columnName: String): java.util.List[Marc] = {
    val ret: java.util.List[Marc] = newArrayList();
    try {
      val marcCodeTexts = getValue(columnName, ColumnConfigurations.MARC_CODE).split(",");
      for (string <- marcCodeTexts) {
        val codes = string.split("-");
        if (codes.length > 2) {
          ret.add(new Marc(codes(0), codes(1), codes(2), null))
        }
      }
    } catch {
      case e: Exception =>
        throw new MarcException(
          "A Marc kódot nem lehet a configurationból beolvasni: "
            + columnName);
    }
    return ret;
  }

  def getRememberingColumns(): java.util.List[String] = {
    val rememberingColumnok: java.util.List[String] = newArrayList()
    for (columnIndex <- 1 until table(0).length) {
      if (isTrue(table(0)(columnIndex), ColumnConfigurations.REMEMBERING)) {
        rememberingColumnok.add(table(0)(columnIndex))
      }
    }
    rememberingColumnok
  }

  def getTable() = table

  def setTable(table: Array[Array[String]]) = this.table = table

}
