package eu.eyan.idakonyvtar.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import eu.eyan.idakonyvtar.controller.IController;
import eu.eyan.idakonyvtar.controller.IControllerMenüvel;

public class DialogHandler
{

    public static <INPUT> void startModalDialog(Component parent, IController<INPUT> controller, INPUT input)
    {
        controller.initData(input);

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

    public static <INPUT> JFrame runInFrame(IControllerMenüvel<INPUT> controller, INPUT input)
    {
        return runInFrame(null, controller, input, controller.getMenuBar());
    }

    @Deprecated
    public static <INPUT> JFrame runInModalerFrame(Component parent, IController<INPUT> controller, INPUT input)
    {
        final Window parentWindow = SwingUtilities.windowForComponent(parent);
        if (parentWindow != null)
        {
            parentWindow.setEnabled(false);
        }
        JFrame frame = runInFrame(parent, controller, input, null);
        frame.addWindowListener(new WindowAdapter()
        {

            @Override
            public void windowClosing(WindowEvent e)
            {
                parentWindow.setEnabled(true);
            }
        });
        return frame;
    }

    private static <INPUT> JFrame runInFrame(Component parent, IController<INPUT> controller, INPUT input, JMenuBar jMenuBar)
    {
        controller.initData(input);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(controller.getView());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int szélesség = controller.getDefaultSize().width;
        int magasság = controller.getDefaultSize().height;
        frame.setSize(szélesség, magasság);
        frame.setLocation((screenSize.width - szélesség) / 2, (screenSize.height - magasság) / 2);
        frame.setTitle(controller.getTitle());
        frame.setJMenuBar(jMenuBar);

        controller.initDataBindings();
        frame.setVisible(true);
        return frame;
    }

}
