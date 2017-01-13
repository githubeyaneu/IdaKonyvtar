package eu.eyan.idakonyvtar.view

import eu.eyan.util.swing.JTextFieldAutocomplete
import eu.eyan.log.Log
import eu.eyan.util.swing.JTextFieldPlus.JTextFieldPlusImplicit

class MultiFieldAutocomplete(columnName: String, hintText: String) extends MultiField[String, JTextFieldAutocomplete](columnName) {

  var autocompleteList: List[String] = List()

  Log.debug(columnName)

  protected def addFieldEditListener(editor: JTextFieldAutocomplete, listener: FieldEditListener[JTextFieldAutocomplete]): Unit =
    editor.addKeyReleasedListener(x => if (editor.getText.nonEmpty) listener.fieldEdited(editor))

  protected def createEditor(): JTextFieldAutocomplete =
    new JTextFieldAutocomplete().setAutocompleteList(autocompleteList).setHintText(hintText)

  protected def getValue(editor: JTextFieldAutocomplete): String = if (editor.getText.equals("")) null else editor.getText

  protected def setValueInEditor(editor: JTextFieldAutocomplete, value: String): Unit = {
    editor.setText(value)
    Log.debug(editor.getName + " " + value)
  }

  def setAutoCompleteList(autocompleteList: List[String]) = this.autocompleteList = autocompleteList
}