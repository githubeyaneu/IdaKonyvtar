package eu.eyan.idakonyvtar.view;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

class MultiFieldJTextField(columnName: String) extends MultiField[String, JTextField](columnName) {

  protected def createEditor() = new JTextField()

  protected def getValue(editor: JTextField) =
    {
      val text = editor.getText().trim()
      if (text.equals("")) null else text
    }

  protected def setValueInEditor(editor: JTextField, value: String) = editor.setText(value)

  protected def addFieldEditListener(editorComponent: JTextField, listener: FieldEditListener[JTextField]) =
    editorComponent.addKeyListener(new KeyAdapter() {
      override def keyReleased(e: KeyEvent) = listener.fieldEdited(editorComponent)
    });
}