package eu.eyan.idakonyvtar.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.jdesktop.swingx.JXTable;

public class BookTable extends JXTable {
	private static final long serialVersionUID = 1L;

	private String emptyText = "Sajnos nem található book.";

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (getRowCount() == 0) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.BLACK);
			g2d.drawString(getEmptyText(), 10, 20);
		}
	}

	public String getEmptyText() {
		return emptyText;
	}

	public void setEmptyText(String emptyText) {
		this.emptyText = emptyText;
	}
}
