package eu.eyan.idakonyvtar.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.jdesktop.swingx.JXTable;

class BookTable extends JXTable {
  var emptyText = "Sajnos nem található könyv."

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
}