package eu.eyan.idakonyvtar.view;

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D

import org.jdesktop.swingx.JXTable

import com.jgoodies.binding.adapter.SingleListSelectionAdapter
import com.jgoodies.binding.list.SelectionInList

import eu.eyan.idakonyvtar.IdaLibrary
import eu.eyan.idakonyvtar.model.ColumnKonfiguration
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
import eu.eyan.util.swing.Col
import eu.eyan.util.swing.Row
import eu.eyan.util.swing.JTableModelPlus
import javax.swing.ListModel

class BookTable(nameOfLibrary: String, columnNames: List[String], books: SelectionInList[_], cellValueGetter: (Row, Col) => String, emptyText: Observable[String]) extends WithComponent {
  def getComponent = scrollPane // FIXME... use another
  def getSelectedIndex = table.convertRowIndexToModel(table.getSelectedRow)
  def onSelectionChanged(action: Int => Unit) = table.onValueChanged(action(table.getSelectedRow))
  def onLineDoubleClicked(action: => Unit) = table.onDoubleClick(action)
  def setAllColumnFilter(textToFilter: String) = { table.setRowFilter(new SpecialCharacterRowFilter(textToFilter)); highlightRenderer.setHighlightText(textToFilter) }

  private val highlightRenderer = new HighlightRenderer
  private val columnWidthsInRegistry = IdaLibrary.registryValue(nameOfLibrary + "_columnWidths ")
  private val table = new JXTableWithEmptyText(emptyText).rememberColumnWidhts(columnWidthsInRegistry)
  private val scrollPane = new JScrollPane(table)

  table.setSelectionModel(new SingleListSelectionAdapter(books.getSelectionIndexHolder))
  table.setModel(new JTableModelPlus(books.asInstanceOf[ListModel[_]], columnNames, cellValueGetter))
  table.setDefaultRenderer(classOf[Object], highlightRenderer)
  table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
}