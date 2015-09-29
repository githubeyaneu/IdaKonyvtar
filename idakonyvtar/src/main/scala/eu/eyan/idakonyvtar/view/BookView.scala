package eu.eyan.idakonyvtar.view;

import com.google.common.collect.Lists.newArrayList
import java.awt.Component
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JTextField
import com.jgoodies.forms.builder.PanelBuilder
import com.jgoodies.forms.factories.CC
import com.jgoodies.forms.layout.FormLayout
import eu.eyan.idakonyvtar.model.ColumnConfigurations
import eu.eyan.idakonyvtar.model.ColumnKonfiguration
import com.jgoodies.forms.layout.RowSpec
import AbstractView._
import scala.collection.mutable.MutableList

object BookView {
  val ISBN_TEXT = "isbnText";
  val ISBN_LABEL = "isbnLabel";
}

class BookView extends AbstractView {

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

      row += addRow(layout, "3dlu, pref")
      panelBuilder.add(isbnSearchLabel, CC.xyw(1, row, 1))
      isbnSearchLabel.setName(BookView.ISBN_LABEL)
      panelBuilder.add(isbnText, CC.xyw(3, row, 1))
      isbnText.setName(BookView.ISBN_TEXT)
    }

    row += addRow(layout, "3dlu, pref")
    panelBuilder.addSeparator("Adatok", CC.xyw(1, row, 3))

    for (i <- 0 until columns.size) {
      row += addRow(layout, "3dlu, pref")
      val columnName = columns(i)
      panelBuilder.addLabel(columnName, CC.xy(1, row))

      val isMultiEditorField = columnConfiguration.isTrue(columnName, ColumnConfigurations.MULTIFIELD)
      val isAutocompleteField = columnConfiguration.isTrue(columnName, ColumnConfigurations.AUTOCOMPLETE)
      val editor: Component =
        if (isAutocompleteField) {
          if (isMultiEditorField) {
            new MultiFieldJComboBox(columnName)
          } else {
            val jComboBox = new JComboBox[String]()
            jComboBox.setEditable(true)
            jComboBox
          }
        } else {
          if (isMultiEditorField) {
            new MultiFieldJTextField(columnName)
          } else {
            new JTextField(20)
          }
        }
      editor.setName(columnName)
      editors += editor
      panelBuilder.add(editor, CC.xy(3, row))
    }
    panelBuilder.build()
  }
}