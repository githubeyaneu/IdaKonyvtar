package eu.eyan.idakonyvtar.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.jdesktop.swingx.JXTable;
import javax.swing.event.ListSelectionEvent
import java.awt.event.MouseAdapter
import javax.swing.event.TableColumnModelListener
import javax.swing.event.ChangeEvent
import javax.swing.event.TableColumnModelEvent
import java.awt.event.MouseEvent
import eu.eyan.util.swing.JTablePlus.JTableImplicit
import eu.eyan.util.swing.SwingPlus
import eu.eyan.idakonyvtar.IdaLibrary
import javax.swing.table.TableModel
import eu.eyan.util.swing.JComponentPlus.JComponentImplicit
import javax.swing.JTable
import eu.eyan.log.Log

class BookTable(nameOfLibrary:String) extends JXTable {
  var emptyText = "Sajnos nem található könyv."
  var lastWidths = ""
  val columnInReg = IdaLibrary.registryValue("columnWidths "+nameOfLibrary)
  //  setAutoResizeMode(JTable.AUTO_RESIZE_OFF)

  override protected def paintComponent(g: Graphics) = {
    super.paintComponent(g)
    if (getRowCount() == 0) {
      val g2d = g.asInstanceOf[Graphics2D]
      g2d.setColor(Color.BLACK)
      val TEXT_X = 10
      val TEXT_Y = 20
      g2d.drawString(emptyText, TEXT_X, TEXT_Y)
    }
  }

  def setEmptyText(emptyText: String) = this.emptyText = emptyText
  def columns = for (i <- 0 until getColumnCount) yield getColumn(i)

  this.getTableHeader.onMouseReleased({
    Log.debug(""+getWidth)
    columnInReg.save(lastWidths)
  })

  this onColumnMarginChanged columnWidthChanged
  def columnWidthChanged = if (getWidth > 0) {
    lastWidths = columns.map(_.getWidth).map(_ * 100 / getWidth).mkString(",")
  }

  override def setModel(dataModel: TableModel) = { super.setModel(dataModel); updateColumnWidths }

  this onComponentResized updateColumnWidths

  def updateColumnWidths = {
    if (columnInReg != null && getWidth > 0)
      columnInReg.read.foreach(widthPercents => {
        val colPercents = widthPercents.split(",").map(_.toInt)
        Log.info(getWidth + "")
        Log.info(colPercents.mkString(","))
        if (colPercents.size == getColumnCount())
          for (i <- 0 until getColumnCount) {
            val width = getWidth * colPercents(i) / 100
            Log.debug(i + " " + width + " ")
            getColumn(i).setPreferredWidth(width)
          }
      })
  }

}