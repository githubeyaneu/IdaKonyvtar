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
      g2d.drawString(emptyText, 10, 20)
    }
  }

  def setEmptyText(emptyText: String) = this.emptyText = emptyText
}