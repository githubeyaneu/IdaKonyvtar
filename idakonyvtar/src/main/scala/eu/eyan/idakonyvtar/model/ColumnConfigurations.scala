package eu.eyan.idakonyvtar.model

object ColumnConfigurations extends Enumeration {

  type ColumnConfigurations = ColumnConfigurationsValue

  case class ColumnConfigurationsValue(name: String) extends Val(name)

  val MULTIFIELD = columnConfigurationsInternalValue("MultiMező")
  val AUTOCOMPLETE = columnConfigurationsInternalValue("AutoComplete")
  val MARC_CODE = columnConfigurationsInternalValue("MarcKód")
  val REMEMBERING = columnConfigurationsInternalValue("Emlékező")
  val SHOW_IN_TABLE = columnConfigurationsInternalValue("Táblázatban")

  protected final def columnConfigurationsInternalValue(name: String): ColumnConfigurationsValue = ColumnConfigurationsValue(name)
}