package eu.eyan.idakonyvtar.view;

import java.awt.Component
import javax.swing.JPanel
import javax.swing.JScrollPane
import com.jgoodies.forms.factories.CC
import com.jgoodies.forms.layout.FormLayout;
import eu.eyan.util.swing.JPanelWithFrameLayout

class LibraryView extends AbstractView {
  private val bookTable = new BookTable
  def getBookTable() = bookTable

  override def createViewComponent(): Component = {
    val panel = new JPanelWithFrameLayout()
      .withBorders
      .withSeparators
      .newColumnFPG
      .newRowFPG
      
    val scrollPane = new JScrollPane(getBookTable())
    panel.add(scrollPane)
    panel
  }
}