package eu.eyan.idakonyvtar.view;

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D

import org.jdesktop.swingx.JXTable

import com.jgoodies.binding.adapter.SingleListSelectionAdapter
import com.jgoodies.binding.list.SelectionInList

import eu.eyan.idakonyvtar.IdaLibrary
import eu.eyan.log.Log
import eu.eyan.util.swing.HighlightRenderer
import eu.eyan.util.swing.JComponentPlus.JComponentImplicit
import eu.eyan.util.swing.JTablePlus.JTableImplicit
import eu.eyan.util.swing.SpecialCharacterRowFilter
import eu.eyan.util.swing.WithComponent
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import javax.swing.table.TableModel
import com.jgoodies.binding.adapter.AbstractTableAdapter
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.util.registry.RegistryValue
import rx.lang.scala.Observable
import rx.lang.scala.subjects.BehaviorSubject
import eu.eyan.util.rx.lang.scala.ObservablePlus.ObservableImplicit
import eu.eyan.util.awt.Graphics2DPlus.Graphics2DImplicit
import eu.eyan.util.swing.JXTableWithEmptyText
import eu.eyan.util.swing.JTableModelPlus
import javax.swing.ListModel
import eu.eyan.util.swing.TableRow
import eu.eyan.util.swing.TableCol
import eu.eyan.idakonyvtar.model.BookField

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