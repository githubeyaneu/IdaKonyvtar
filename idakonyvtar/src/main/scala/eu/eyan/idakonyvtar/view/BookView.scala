package eu.eyan.idakonyvtar.view;

import java.awt.Component

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
    val layout = new FormLayout("pref, 3dlu, pref:grow")
    val panelBuilder = new PanelBuilder(layout)

    var row = 0

    if (isbnEnabled) {
      row += addRow(layout, "pref")
      panelBuilder.addSeparator("Isbn", CC.xyw(1, row, 3))

      row += addRow(layout, SEPARATOR_PREF)
      panelBuilder.add(isbnSearchLabel, CC.xyw(1, row, 1))
      isbnSearchLabel.setName(BookView.ISBN_LABEL)
      panelBuilder.add(isbnText, CC.xyw(3, row, 1))
      isbnText.setName(BookView.ISBN_TEXT)
    }

    row += addRow(layout, SEPARATOR_PREF)
    panelBuilder.addSeparator("Adatok", CC.xyw(1, row, 3))

    for {i <- 0 until columns.size} {
      row += addRow(layout, SEPARATOR_PREF)
      val columnName = columns(i)
      panelBuilder.addLabel(columnName, CC.xy(1, row))

      val isMultiEditorField = columnConfiguration.isTrue(columnName, ColumnConfigurations.MULTIFIELD)
      val isAutocompleteField = columnConfiguration.isTrue(columnName, ColumnConfigurations.AUTOCOMPLETE)
      val editor: Component =
        if (isAutocompleteField) {
        	if (isMultiEditorField) {
                new MultiFieldAutocomplete(columnName, "Autocomplete", "Nincs találat")
              }
              else {
                new JTextFieldAutocomplete().setHintText("Autocomplete").setNoItemsFoundText("Nincs találat")
              }
        	
          if (isMultiEditorField) {
            new MultiFieldAutocomplete(columnName, "Autocomplete", "Nincs találat")
          } else {
            new Autocomplete().setHintText("Autocomplete").setNoItemsFoundText("Nincs találat")
          }
          else {
            new JTextFieldAutocomplete().setHintText("Autocomplete")
          }
        }
        else {
          if (isMultiEditorField) {
            new MultiFieldJTextField(columnName)
          }
          else {
            new JTextField(TEXTFIELD_DEFAULT_SIZE)
          }
        }
      editor.setName(columnName)
      editors += editor
      panelBuilder.add(editor, CC.xy(3, row))
    }
    panelBuilder.build()
  }
}