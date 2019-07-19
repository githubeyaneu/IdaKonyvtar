package eu.eyan.idakonyvtar.view

import com.jgoodies.binding.adapter.SingleListSelectionAdapter
import com.jgoodies.binding.list.SelectionInList
import eu.eyan.idakonyvtar.IdaLibrary
import eu.eyan.idakonyvtar.model.BookField
import eu.eyan.util.swing.JTablePlus.JTableImplicit
import eu.eyan.util.swing._
import javax.swing.{JScrollPane, ListModel, ListSelectionModel}
import rx.lang.scala.Observable

class BookTable(
    private val nameOfLibrary: String, 
    private val columnNames: List[BookField], 
    private val books: SelectionInList[_], 
    private val cellValueGetter: (TableRow, TableCol) => String, 
    private val emptyText: Observable[String]) {
  
  def getComponent = scrollPane // TODO... use another
  def getSelectedIndex = table.convertRowIndexToModel(table.getSelectedRow)
  def onSelectionChanged(action: Int => Unit) = table.onValueChanged(action(table.getSelectedRow))
  def onLineDoubleClicked(action: => Unit) = table.onDoubleClick(action)
  def setAllColumnFilter(textToFilter: String) = { table.setRowFilter(new SpecialCharacterRowFilter(textToFilter)); highlightRenderer.setHighlightText(textToFilter) }

  private val highlightRenderer = new HighlightRenderer
  private val columnWidthsInRegistry = IdaLibrary.registryValue(nameOfLibrary + "_columnWidths ")
  private val table = new JXTableWithEmptyText(emptyText).rememberColumnWidhts(columnWidthsInRegistry)
  private val scrollPane = new JScrollPane(table)

  table.setSelectionModel(new SingleListSelectionAdapter(books.getSelectionIndexHolder))
  table.setModel(new JTableModelPlus(books.asInstanceOf[ListModel[_]], columnNames.map(_.fieldName), cellValueGetter))
  table.setDefaultRenderer(classOf[Object], highlightRenderer)
  table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
}