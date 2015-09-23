package eu.eyan.idakonyvtar.view;

import com.google.common.collect.Lists.newArrayList
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.List
import javax.swing.JComboBox
import javax.swing.JTextField
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import javax.swing.ComboBoxModel

class MultiFieldJComboBox(columnName: String) extends MultiField[String, JComboBox[String]](columnName) {
  var columnList: java.util.List[String] = newArrayList()

  protected def addFieldEditListener(editor: JComboBox[String], listener: FieldEditListener[JComboBox[String]]) = {
    editor.getEditor().getEditorComponent().asInstanceOf[JTextField].addKeyListener(new KeyAdapter() {
      override def keyReleased(e: KeyEvent) = listener.fieldEdited(editor)
    })
  }

  protected def getEditor() = {
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

  def setAutoCompleteList(columnList: java.util.List[String]) = this.columnList = columnList
}