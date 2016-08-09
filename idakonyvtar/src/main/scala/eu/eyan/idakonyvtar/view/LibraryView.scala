package eu.eyan.idakonyvtar.view;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

class LibraryView extends AbstractView {
  private val bookTable = new BookTable();
  def getBookTable() = bookTable

  override def createViewComponent(): Component = {
    val panel = new JPanel(new FormLayout("pref:grow", "pref, pref:grow"))
    val scrollPane = new JScrollPane(getBookTable())
    panel.add(scrollPane, CC.xy(1, 2))
    scrollPane
  }
}