package eu.eyan.idakonyvtar.view;

import java.awt.Component

import scala.collection.mutable.MutableList

<<<<<<< HEAD
=======
import scala.collection.mutable.MutableList
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
import com.jgoodies.forms.builder.PanelBuilder
import com.jgoodies.forms.factories.CC
import com.jgoodies.forms.layout.FormLayout
<<<<<<< HEAD

=======
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
import AbstractView.addRow
import eu.eyan.idakonyvtar.model.ColumnConfigurations
import eu.eyan.idakonyvtar.model.ColumnKonfiguration
import eu.eyan.util.swing.JTextFieldAutocomplete
import javax.swing.JLabel
import javax.swing.JTextField
<<<<<<< HEAD
=======
import eu.eyan.util.swing.JPanelWithFrameLayout
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git

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
    val panel = new JPanelWithFrameLayout().withSeparators
    panel.newColumn.newColumn("pref:grow")

    if (isbnEnabled) {
<<<<<<< HEAD
      row += addRow(layout, "pref")
      panelBuilder.addSeparator("Isbn", CC.xyw(1, row, 3))

      row += addRow(layout, SEPARATOR_PREF)
      panelBuilder.add(isbnSearchLabel, CC.xyw(1, row, 1))
      isbnSearchLabel.setName(BookView.ISBN_LABEL)
      panelBuilder.add(isbnText, CC.xyw(3, row, 1))
      isbnText.setName(BookView.ISBN_TEXT)
=======
      panel.newRow.span.addSeparatorWithTitle("Isbn")
      panel.newRow
      panel.add(isbnSearchLabel.withName(BookView.ISBN_LABEL))
      panel.nextColumn.add(isbnText.withName(BookView.ISBN_TEXT))
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
    }

<<<<<<< HEAD
    row += addRow(layout, SEPARATOR_PREF)
    panelBuilder.addSeparator("Adatok", CC.xyw(1, row, 3))
=======
    panel.newRow.span.addSeparatorWithTitle("Adatok")
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git

<<<<<<< HEAD
    for {i <- 0 until columns.size} {
      row += addRow(layout, SEPARATOR_PREF)
=======
    for { i <- 0 until columns.size } {
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
      val columnName = columns(i)
      panel.newRow.addLabel(columnName)

      val isMultiEditorField = columnConfiguration.isTrue(columnName, ColumnConfigurations.MULTIFIELD)
      val isAutocompleteField = columnConfiguration.isTrue(columnName, ColumnConfigurations.AUTOCOMPLETE)
      val editor: Component =
        if (isAutocompleteField) {
<<<<<<< HEAD
          if (isMultiEditorField) {
            new MultiFieldAutocomplete(columnName, "Autocomplete")
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
=======
          if (isMultiEditorField) new MultiFieldAutocomplete(columnName, "Autocomplete", "Nincs találat")
          else new JTextFieldAutocomplete().setHintText("Autocomplete")
        } else {
          if (isMultiEditorField) new MultiFieldJTextField(columnName)
          else new JTextField(TEXTFIELD_DEFAULT_SIZE)
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
        }

      editor.setName(columnName)
      panel.nextColumn.add(editor)
      editors += editor
    }

    panel
  }
}