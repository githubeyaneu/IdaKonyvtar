package eu.eyan.idakonyvtar.view;

import com.google.common.collect.Lists.newArrayList;

import java.awt.Component;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import eu.eyan.idakonyvtar.model.ColumnConfigurations;
import eu.eyan.idakonyvtar.model.ColumnKonfiguration;

object BookView {
  val ISBN_TEXT = "isbnText";
  val ISBN_LABEL = "isbnLabel";
}

class BookView extends AbstractView {

  var columns: java.util.List[String] = newArrayList()

  var isbnEnabled: Boolean = false

  val editors: java.util.List[Component] = newArrayList()

  val isbnSearchLabel = new JLabel()

  val isbnText = new JTextField()

  var columnConfiguration: ColumnKonfiguration = null

  def getEditors() = editors

  def getIsbnSearchLabel() = isbnSearchLabel

  def getIsbnText() = isbnText

  def setIsbnEnabled(isbnEnabled: Boolean): Unit = { this.isbnEnabled = isbnEnabled }

  def setColumnConfiguration(columnConfiguration: ColumnKonfiguration) = { this.columnConfiguration = columnConfiguration }

  def setColumns(columns: java.util.List[String]) = this.columns = columns

  protected override def createViewComponent(): Component = {
    var rowSpec = ""
    if (isbnEnabled) {
      rowSpec += "pref, 3dlu, pref, 3dlu, "
    }
    rowSpec += rowSpec + "pref"

    for (i <- 0 until columns.size()) rowSpec += ",3dlu ,pref"

    val panelBuilder = new PanelBuilder(new FormLayout("pref, 3dlu, pref:grow", rowSpec))

    var row = 1

    if (isbnEnabled) {
      panelBuilder.addSeparator("Isbn", CC.xyw(1, row, 3))
      row = row + 2
      panelBuilder.add(isbnSearchLabel, CC.xyw(1, row, 1))
      isbnSearchLabel.setName(BookView.ISBN_LABEL)
      panelBuilder.add(isbnText, CC.xyw(3, row, 1))
      isbnText.setName(BookView.ISBN_TEXT)
      row = row + 2
    }

    panelBuilder.addSeparator("Adatok", CC.xyw(1, row, 3))

    for (i <- 0 until columns.size()) {
      row = row + 2;
      val columnName = columns.get(i)
      panelBuilder.addLabel(columnName, CC.xy(1, row))

      var editor: Component = null
      val multi = columnConfiguration.isTrue(columnName, ColumnConfigurations.MULTIFIELD)
      if (columnConfiguration.isTrue(columnName, ColumnConfigurations.AUTOCOMPLETE)) {
        if (multi) {
          editor = new MultiFieldJComboBox(columnName)
        } else {
          val jComboBox = new JComboBox[String]()
          jComboBox.setEditable(true)
          editor = jComboBox
        }
      } else {
        if (multi) {
          editor = new MultiFieldJTextField(columnName)
        } else {
          editor = new JTextField(20)
        }
      }
      editor.setName(columnName)
      editors.add(editor)
      panelBuilder.add(editor, CC.xy(3, row))
    }
    panelBuilder.build()
  }
}