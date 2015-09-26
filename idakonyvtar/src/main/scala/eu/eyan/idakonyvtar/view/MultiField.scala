package eu.eyan.idakonyvtar.view;

import com.google.common.collect.Lists.newArrayList
import java.awt.Component
import java.awt.event.ActionEvent
import java.util.List
import java.util.stream.Collectors
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import com.jgoodies.forms.builder.PanelBuilder
import com.jgoodies.forms.factories.CC
import com.jgoodies.forms.layout.FormLayout
import scala.collection.JavaConversions._
import java.awt.event.ActionListener

abstract class MultiField[INPUT, EDITOR <: Component](columnName: String) extends JPanel with FieldEditListener[EDITOR] {

  protected def addFieldEditListener(editor: EDITOR, listener: FieldEditListener[EDITOR])
  protected def getEditor(): EDITOR

  /**
   * @return null if empty!
   */
  protected def getValue(editor: EDITOR): INPUT

  protected def setValueInEditor(editor: EDITOR, value: INPUT)

  class Field[EDITOR](val editor: EDITOR, val delete: JButton, val panel: JPanel)

  val fields: java.util.List[Field[EDITOR]] = newArrayList()
  var counter = 1
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

  def setValues(values: java.util.List[INPUT]) = {
    removeAll()
    fields.clear()

    for (input <- values) addEditor(input, false)

    addEditor(null.asInstanceOf[INPUT], true)
  }

  private def addEditor(input: INPUT, last: Boolean) = {
    val editor = getEditor()
    addFieldEditListener(editor, this)
    val deleteButton = new JButton("x")

    val panelBuilder = new PanelBuilder(new FormLayout("f:p:g, 3dlu, 30dlu", "f:p:g, 3dlu"))
    panelBuilder.add(editor, CC.xy(1, 1))
    panelBuilder.add(deleteButton, CC.xy(3, 1))
    val fieldPanel = panelBuilder.build()

    if (last)
      deleteButton.setEnabled(false)
    else
      setValueInEditor(editor, input)

    val field = new Field[EDITOR](editor, deleteButton, fieldPanel)
    deleteButton.addActionListener(new ActionListener {
      override def actionPerformed(actionEvent: ActionEvent) = {
        fields.remove(field)
        remove(field.panel)
        revalidate()
      }
    })
    fields.add(field);
    add(fieldPanel);

    fieldPanel.setName(columnName + ".panel." + counter);
    editor.setName(columnName + counter);
    deleteButton.setName(columnName + ".delete." + counter);
    counter = counter + 1

    revalidate();
    // SwingUtilities.getWindowAncestor(this).pack();
  }

  def fieldEdited(source: EDITOR) = {
    val lastField = fields.get(fields.size() - 1)
    if (lastField.editor == source) {
      lastField.delete.setEnabled(true)
      addEditor(null.asInstanceOf[INPUT], true);
    }
  }

  def getValues(): java.util.List[INPUT] = fields.map(field => getValue(field.editor))
}