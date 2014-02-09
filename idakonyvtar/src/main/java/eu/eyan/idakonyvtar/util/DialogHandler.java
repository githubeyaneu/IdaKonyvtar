package eu.eyan.idakonyvtar.util;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import eu.eyan.idakonyvtar.controller.IController;

public class DialogHandler
{

    public static <MODEL> void modalDialog(Component parent, IController<MODEL> controller, MODEL inputData)
    {
        controller.initData(inputData);

        Window parentWindow = SwingUtilities.windowForComponent(parent);
        JDialog dialog = new JDialog(parentWindow);
        dialog.setLocationRelativeTo(parent);
        dialog.setModal(true);
        dialog.add(controller.getView());
        dialog.setTitle(controller.getTitle());
        dialog.setResizable(true);
        dialog.setSize(controller.getDefaultSize());
        dialog.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.out.println("Closing");
                super.windowClosed(e);
            }
        });

        controller.initDataBindings();
        dialog.setVisible(true);
        parentWindow.invalidate();
    }

}
