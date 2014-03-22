package eu.eyan.idakonyvtar.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import lombok.Getter;
import lombok.Setter;

import org.jdesktop.swingx.JXTable;

public class KönyvTábla extends JXTable
{
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String üresSzöveg = "Sajnos nem található könyv.";

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (getRowCount() == 0)
        {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.BLACK);
            g2d.drawString(üresSzöveg, 10, 20);
        }
    }
}
