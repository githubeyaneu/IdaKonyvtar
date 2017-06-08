package eu.eyan.idakonyvtar.view;

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JComboBox
import javax.swing.JTextField
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import javax.swing.ComboBoxModel
import eu.eyan.log.Log

class MultiFieldJComboBox(columnName: String) extends MultiField[String, JComboBox[String]](columnName) {
  var columnList: java.util.List[String] = com.google.common.collect.Lists.newArrayList() // java.util because of dependencies

  protected def addFieldEditListener(editor: JComboBox[String], listener: FieldEditListener[JComboBox[String]]) = {
    editor.getEditor().getEditorComponent().asInstanceOf[JTextField].addKeyListener(new KeyAdapter() {
      override def keyReleased(e: KeyEvent) = listener.fieldEdited(editor)
    })
  }

  protected def createEditor() = {
    val jComboBox = new JComboBox[String]()
    jComboBox.setModel(new ListComboBoxModel[String](columnList).asInstanceOf[ComboBoxModel[String]])

    // It is important: first setEditable then decorate, because this AS is a piece of shit
    jComboBox.setEditable(true)
    AutoCompleteDecorator.decorate(jComboBox)
    // It is important: first setEditable then decorate, because this AS is a piece of shit

    jComboBox
  }

  def getValue(editor: JComboBox[String]) = {
    val text = editor.getEditor().getEditorComponent().asInstanceOf[JTextField].getText().trim()
    if (text.equals("")) null else text
  }

  def setValueInEditor(editor: JComboBox[String], value: String) =
    editor.getEditor().getEditorComponent().asInstanceOf[JTextField].setText(value)

  def setAutoCompleteList(autocompleteList: java.util.List[String]) = {
    Log.debug(" " + autocompleteList)
    this.columnList = autocompleteList
  }
}