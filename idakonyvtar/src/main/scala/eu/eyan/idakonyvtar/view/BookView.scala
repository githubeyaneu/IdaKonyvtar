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
import eu.eyan.log.Log
import com.github.sarxos.webcam.WebcamPanel
import com.github.sarxos.webcam.WebcamPicker
import com.github.sarxos.webcam.WebcamResolution
import java.lang.Thread.UncaughtExceptionHandler
import javax.swing.JPanel

object BookView {
  val ISBN_TEXT = "isbnText";
  val ISBN_LABEL = "isbnLabel";
}

class BookView extends AbstractView {
  val TEXTFIELD_DEFAULT_SIZE = 20
  val SEPARATOR_PREF = "3dlu, pref"

  val editors: MutableList[Component] = MutableList()
  val images: MutableList[JLabel] = MutableList()
  val isbnSearchLabel = new JLabel()
  val isbnText = new JTextField()

  var columns: List[String] = null
  def setColumns(columns: List[String]) = this.columns = columns

  var isbnEnabled: Boolean = false
  def setIsbnEnabled(isbnEnabled: Boolean): Unit = this.isbnEnabled = isbnEnabled

  var columnConfiguration: ColumnKonfiguration = null
  def setColumnConfiguration(columnConfiguration: ColumnKonfiguration) = this.columnConfiguration = columnConfiguration

  var webcamPanel = new JPanelWithFrameLayout 


  
  protected override def createViewComponent(): Component = {
    val rowsPanel = new JPanelWithFrameLayout().withSeparators
    rowsPanel.newColumn.newColumn("pref:grow")
    val imagesPanel = new JPanelWithFrameLayout().name("imagesPanel").withSeparators.newColumn("320px")

    

    if (isbnEnabled) {
      rowsPanel.newRow.span.addSeparatorWithTitle("Isbn")
      rowsPanel.newRow
      rowsPanel.add(isbnSearchLabel.name(BookView.ISBN_LABEL))
      rowsPanel.nextColumn.add(isbnText.name(BookView.ISBN_TEXT))
    }

    rowsPanel.newRow.span.addSeparatorWithTitle("Adatok")

    for { i <- 0 until columns.size } {
      val columnName = columns(i)
      Log.debug(s"column $columnName")
      rowsPanel.newRow.addLabel(columnName)

      val isMultiEditorField = columnConfiguration.isTrue(columnName, ColumnConfigurations.MULTIFIELD)
      val isAutocompleteField = columnConfiguration.isTrue(columnName, ColumnConfigurations.AUTOCOMPLETE)
      val isPictureField = columnConfiguration.isTrue(columnName, ColumnConfigurations.PICTURE)

      val editor: Component =
        if (isPictureField) {
          val panel = JPanelWithFrameLayout()
          imagesPanel.newRow.addLabel(columnName)
          val imageLabel = imagesPanel.newRow.addLabel("").name("look" + columnName)
          images += imageLabel
          panel.newColumn.addTextField("", TEXTFIELD_DEFAULT_SIZE).name("picturePath")
          panel.newColumn.addButton("katt").name("click")
          panel
        } else if (isAutocompleteField) {
          if (isMultiEditorField) new MultiFieldAutocomplete(columnName, "Autocomplete", "Nincs találat")
          else new JTextFieldAutocomplete().setHintText("Autocomplete")
        } else {
          if (isMultiEditorField) new MultiFieldJTextField(columnName)
          else new JTextField(TEXTFIELD_DEFAULT_SIZE)
        }

      editor.setName(columnName)
      rowsPanel.nextColumn.add(editor)
      editors += editor
    }

    val panel = new JPanelWithFrameLayout().withSeparators
    panel.newColumn("f:p").add(rowsPanel)
    panel.newColumn("f:320px").add(imagesPanel)
    panel.newColumn("f:320px").add(webcamPanel)
    panel
  }
}