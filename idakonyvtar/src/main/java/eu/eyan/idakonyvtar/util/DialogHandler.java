package eu.eyan.idakonyvtar.util;

import java.awt.Component;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import eu.eyan.idakonyvtar.controller.IController;

public class DialogHandler
{

    public static void modalDialog(Component parent, IController controller)
    {
        Window parentWindow = SwingUtilities.windowForComponent(parent);
        JDialog dialog = new JDialog(parentWindow);
        dialog.setLocationRelativeTo(parent);
        dialog.setModal(true);
        dialog.add(controller.getView());
        dialog.setTitle(controller.getTitle());
        dialog.setResizable(true);
        dialog.pack();
        dialog.setVisible(true);
        dialog.setSize(controller.getSize());
    }

}
