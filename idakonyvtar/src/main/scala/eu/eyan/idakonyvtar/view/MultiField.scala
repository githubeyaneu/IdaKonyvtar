package eu.eyan.idakonyvtar.view;

import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.mutable.ListBuffer
import com.jgoodies.forms.builder.PanelBuilder
import com.jgoodies.forms.factories.CC
import com.jgoodies.forms.layout.FormLayout
import eu.eyan.log.Log
import eu.eyan.util.awt.AwtHelper
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities
import eu.eyan.util.swing.JPanelWithFrameLayout
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit

abstract class MultiField[INPUT, EDITOR <: Component](columnName: String) extends JPanel with FieldEditListener[EDITOR] {

  protected def addFieldEditListener(editor: EDITOR, listener: FieldEditListener[EDITOR]): Unit
  protected def createEditor(): EDITOR

  /**
   * @return null if empty!
   */
  protected def getValue(editor: EDITOR): INPUT

  protected def setValueInEditor(editor: EDITOR, value: INPUT): Unit

  class Field[EDITOR](val editor: EDITOR, val delete: JButton, val panel: JPanel)

  val fields: ListBuffer[Field[EDITOR]] = ListBuffer()
  var counter = 1
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

  def setValues(values: java.util.List[INPUT]) = {
    Log.debug(getName + ": " + values.mkString(", "))
    removeAll()
    fields.clear()

    for { input <- values } addEditor(input, false)

    addEditor(null.asInstanceOf[INPUT], true)
  }

  private def addEditor(input: INPUT, last: Boolean) = {
    val editor = createEditor().withName(columnName + counter)
    addFieldEditListener(editor, this)
    if (!last) setValueInEditor(editor, input)

    val fieldPanel = new JPanelWithFrameLayout("f:p:g").withName(columnName + ".panel." + counter)
    fieldPanel.newColumn("f:p:g")
    fieldPanel.add(editor)

    fieldPanel.newColumn("30dlu")
    val deleteButton = fieldPanel.addButton("x").withName(columnName + ".delete." + counter).withEnabled(!last)

    fieldPanel.newRowSeparator()

    val field = new Field[EDITOR](editor, deleteButton, fieldPanel)
    deleteButton.onAction { () => { fields -= field; remove(field.panel); revalidate } }
    fields += field
    add(fieldPanel)

    Log.debug(columnName + counter)
    counter = counter + 1

    revalidate
    AwtHelper.tryToEnlargeWindow(SwingUtilities.windowForComponent(this))
  }

  def fieldEdited(source: EDITOR) = {
    val lastField = fields(fields.size() - 1)
    if (lastField.editor == source) {
      lastField.delete.setEnabled(true)
      addEditor(null.asInstanceOf[INPUT], true)
    }
  }

  def getValues(): java.util.List[INPUT] = fields.map(field => getValue(field.editor))
}