package eu.eyan.idakonyvtar.view;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import eu.eyan.util.swing.JTextFieldPlus.JTextFieldPlusImplicit

class MultiFieldJTextField(columnName: String) extends MultiField[String, JTextField](columnName) {

  protected def createEditor(fieldEdited: JTextField => Unit) = {
    val editor = new JTextField()
    editor.onKeyReleased(fieldEdited(editor))
    editor
  }

  protected def getValue(editor: JTextField) = {
    val text = editor.getText.trim
    if (text.isEmpty) None else Some(text)
  }

  protected def setValueInEditor(editor: JTextField)(value: String) = editor.setText(value)
}