package eu.eyan.idakonyvtar.view;

import java.awt.Component

import scala.collection.mutable.MutableList

import scala.collection.mutable.MutableList
import com.jgoodies.forms.builder.PanelBuilder
import com.jgoodies.forms.factories.CC
import com.jgoodies.forms.layout.FormLayout
import AbstractView.addRow
import eu.eyan.idakonyvtar.model.ColumnConfigurations
import eu.eyan.idakonyvtar.model.ColumnKonfiguration
import eu.eyan.util.swing.JTextFieldAutocomplete
import javax.swing.JLabel
import javax.swing.JTextField
import eu.eyan.util.swing.JPanelWithFrameLayout
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit

object BookView {
  val ISBN_TEXT = "isbnText";
  val ISBN_LABEL = "isbnLabel";
}

class BookView extends AbstractView {
  val TEXTFIELD_DEFAULT_SIZE = 20
  val SEPARATOR_PREF = "3dlu, pref"

  val editors: MutableList[Component] = MutableList()
  val isbnSearchLabel = new JLabel()
  val isbnText = new JTextField()

  var columns: List[String] = null
  def setColumns(columns: List[String]) = this.columns = columns

  var isbnEnabled: Boolean = false
  def setIsbnEnabled(isbnEnabled: Boolean): Unit = this.isbnEnabled = isbnEnabled

  var columnConfiguration: ColumnKonfiguration = null
  def setColumnConfiguration(columnConfiguration: ColumnKonfiguration) = this.columnConfiguration = columnConfiguration

  protected override def createViewComponent(): Component = {
    val panel = new JPanelWithFrameLayout()
    panel.newColumn.newColumn("pref:grow")

    if (isbnEnabled) {
      panel.newRow.span.addSeparatorWithTitle("Isbn")
      panel.newRow
      panel.add(isbnSearchLabel.withName(BookView.ISBN_LABEL))
      panel.nextColumn.add(isbnText.withName(BookView.ISBN_TEXT))
    }

    panel.newRow.span.addSeparatorWithTitle("Adatok")

    for { i <- 0 until columns.size } {
      val columnName = columns(i)
      panel.newRow.addLabel(columnName)

      val isMultiEditorField = columnConfiguration.isTrue(columnName, ColumnConfigurations.MULTIFIELD)
      val isAutocompleteField = columnConfiguration.isTrue(columnName, ColumnConfigurations.AUTOCOMPLETE)
      val editor: Component =
        if (isAutocompleteField) {
          if (isMultiEditorField) new MultiFieldAutocomplete(columnName, "Autocomplete")
          else new JTextFieldAutocomplete().setHintText("Autocomplete")
        } else {
          if (isMultiEditorField) new MultiFieldJTextField(columnName)
          else new JTextField(TEXTFIELD_DEFAULT_SIZE)
        }

      editor.setName(columnName)
      panel.nextColumn.add(editor)
      editors += editor
    }

    panel
  }
}
