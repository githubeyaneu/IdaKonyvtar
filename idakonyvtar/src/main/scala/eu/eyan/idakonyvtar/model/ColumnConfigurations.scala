package eu.eyan.idakonyvtar.model

object ColumnConfigurations extends Enumeration {

  type ColumnConfigurations = ColumnConfigurationsValue

  case class ColumnConfigurationsValue(name: String) extends Val(name) {
    def getName() = name
  }

  //  val CANCEL_BUTTON = ControlTextInternalValue("Cancel", "Cancel the changes and dismiss the dialog")
  //  val OK_BUTTON = ControlTextInternalValue("OK", "Save the changes and dismiss the dialog")
  val MULTIFIELD = ColumnConfigurationsInternalValue("MultiMező")
  val AUTOCOMPLETE = ColumnConfigurationsInternalValue("AutoComplete")
  val MARC_CODE = ColumnConfigurationsInternalValue("MarcKód")
  val REMEMBERING = ColumnConfigurationsInternalValue("Emlékező")
  val SHOW_IN_TABLE = ColumnConfigurationsInternalValue("Táblázatban")

  protected final def ColumnConfigurationsInternalValue(name: String): ColumnConfigurationsValue =
    ColumnConfigurationsValue(name)

}