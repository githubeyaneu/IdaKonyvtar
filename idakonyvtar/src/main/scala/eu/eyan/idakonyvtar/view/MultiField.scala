package eu.eyan.idakonyvtar.view;

import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.mutable.ListBuffer

<<<<<<< HEAD
=======
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.mutable.ListBuffer
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
import com.jgoodies.forms.builder.PanelBuilder
import com.jgoodies.forms.factories.CC
import com.jgoodies.forms.layout.FormLayout
<<<<<<< HEAD

import eu.eyan.log.Log
import eu.eyan.util.awt.AwtHelper
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities
=======
import eu.eyan.log.Log
import eu.eyan.util.awt.AwtHelper
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities
import eu.eyan.util.swing.JPanelWithFrameLayout
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git

abstract class MultiField[INPUT, EDITOR <: Component](columnName: String) extends JPanel with FieldEditListener[EDITOR] {

  protected def addFieldEditListener(editor: EDITOR, listener: FieldEditListener[EDITOR]): Unit
<<<<<<< HEAD
  protected def getEditor(): EDITOR
=======
  protected def createEditor(): EDITOR
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git

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

<<<<<<< HEAD
    for {input <- values} addEditor(input, false)
=======
    for { input <- values } addEditor(input, false)
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git

    addEditor(null.asInstanceOf[INPUT], true)
  }

  private def addEditor(input: INPUT, last: Boolean) = {
    val editor = createEditor().withName(columnName + counter)
    addFieldEditListener(editor, this)
    if (!last) setValueInEditor(editor, input)

    val fieldPanel = new JPanelWithFrameLayout().withSeparators.newColumn("f:p:g").newRow("f:p:g").withName(columnName + ".panel." + counter)
    fieldPanel.add(editor)

    fieldPanel.newColumn("30dlu")
    val deleteButton = fieldPanel.addButton("x").withName(columnName + ".delete." + counter).enabled(!last)

    val field = new Field[EDITOR](editor, deleteButton, fieldPanel)
    deleteButton.onAction { () => { fields -= field; remove(field.panel); revalidate } }
    fields += field
    add(fieldPanel)

<<<<<<< HEAD
    fieldPanel.setName(columnName + ".panel." + counter)
    editor.setName(columnName + counter)
    Log.debug(columnName + counter)
    deleteButton.setName(columnName + ".delete." + counter)
=======
    Log.debug(columnName + counter)
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
    counter = counter + 1

<<<<<<< HEAD
    revalidate()
    // SwingUtilities.windowForComponent(this).pack()
=======
    revalidate
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
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