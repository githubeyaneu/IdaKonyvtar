package eu.eyan.idakonyvtar.view

import eu.eyan.util.swing.Autocomplete
import eu.eyan.log.Log

class MultiFieldAutocomplete(columnName: String, hintText: String) extends MultiField[String, Autocomplete](columnName) {

  var autocompleteList: List[String] = List()

  Log.debug(columnName)

  protected def addFieldEditListener(editor: Autocomplete, listener: FieldEditListener[Autocomplete]): Unit =
    editor.addKeyReleasedListener(x => if (editor.getText.nonEmpty) listener.fieldEdited(editor))

  protected def getEditor(): Autocomplete =
    new Autocomplete().setValues(autocompleteList).setHintText(hintText)

  protected def getValue(editor: Autocomplete): String = if (editor.getText.equals("")) null else editor.getText

  protected def setValueInEditor(editor: Autocomplete, value: String): Unit = {
    editor.setText(value)
    Log.debug(editor.getName + " " + value)
  }

  def setAutoCompleteList(autocompleteList: List[String]) = this.autocompleteList = autocompleteList
}