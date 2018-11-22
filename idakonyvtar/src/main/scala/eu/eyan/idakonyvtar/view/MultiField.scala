package eu.eyan.idakonyvtar.view;

import java.awt.Component

import scala.collection.mutable.ListBuffer

import eu.eyan.log.Log
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.JPanelWithFrameLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit

abstract class MultiField[INPUT, EDITOR <: Component](columnName: String) extends JPanel {

  def getValues: List[INPUT] = editors.map(_.editor).map(getValue).toList.filter(_.nonEmpty).map(_.get)

  def setValues(values: Array[INPUT]) = {
    Log.debug(getName + ": " + values.mkString(", "))
    removeAll
    editors.clear

    values foreach addEditorWithValue
    addEditorEmpty
  }

  protected def createEditor(fieldEdited: EDITOR => Unit): EDITOR
  protected def getValue(editor: EDITOR): Option[INPUT]
  protected def setValueInEditor(editor: EDITOR)(value: INPUT): Unit

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  private val editors: ListBuffer[Editor[EDITOR]] = ListBuffer()
  private var counter = 0

  private case class Editor[EDITOR](val editor: EDITOR, val deleteButton: JButton)

  private def addEditorEmpty = addEditor(None)
  private def addEditorWithValue(input: INPUT) = addEditor(Some(input))

  private def addEditor(input: Option[INPUT]): Unit = {
    counter = counter + 1
    val editor = createEditor(addEditorIfLast).name(columnName + counter)
    input.foreach( setValueInEditor(editor) )

    val fieldPanel = new JPanelWithFrameLayout()
      .withSeparators
      .newColumn("f:p:g")
      .newRow("f:p:g")
      .name(columnName + ".panel." + counter)
      .addFluent(editor)
      .newColumn("30dlu")

    val deleteButton = fieldPanel
      .addButton("x")
      .name(columnName + ".delete." + counter)
      .enabled(input.nonEmpty)
      .onAction(removeEditor(editor))

    editors += new Editor[EDITOR](editor, deleteButton)
    add(fieldPanel)

    Log.debug(columnName + counter)
  }

  private def removeEditor(editor: EDITOR) = {
    editors.find(_.editor == editor).foreach(field => editors -= field)
    remove(editor.getParent)
  }

  private def addEditorIfLast(editor: EDITOR) =
    if (editors.last.editor == editor) {
      editors.last.deleteButton.enabled
      addEditorEmpty
    }
}