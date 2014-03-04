package eu.eyan.idakonyvtar.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXFrame;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import eu.eyan.idakonyvtar.controller.IController;
import eu.eyan.idakonyvtar.controller.IControllerMenüvel;
import eu.eyan.idakonyvtar.controller.IDialogController;

public class DialogHelper
{

    public static final String MÉGSEM = "Mégsem";
    public static final String MENTÉS = "Mentés";

    private static class MyDialog extends JDialog
    {
        public MyDialog(Window owner)
        {
            super(owner);
        }

        private static final long serialVersionUID = 1L;
        private boolean ok = false;
    }

    public static <INPUT, OUTPUT> boolean startModalDialog(Component parent, IDialogController<INPUT, OUTPUT> controller, INPUT input)
    {
        controller.initData(input);

        Window parentWindow = SwingUtilities.windowForComponent(parent);
        MyDialog dialog = new MyDialog(parentWindow);
        dialog.setLocationRelativeTo(parent);
        dialog.setModal(true);
        PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("pref:grow", "top:pref:grow, 3dlu, pref"));
        panelBuilder.add(controller.getView(), CC.xy(1, 1));
        panelBuilder.add(getButtons(dialog, controller), CC.xy(1, 3));
        dialog.add(addScrollableInBorders(panelBuilder.build()));
        dialog.setTitle(controller.getTitle());
        dialog.setResizable(true);
        dialog.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.out.println("Closing");
                super.windowClosed(e);
            }
        });

        controller.initBindings();
        dialog.pack();
        középretesz(dialog);
        controller.addResizeListener(dialog);

        // blockiert:
        dialog.setVisible(true);
        parentWindow.invalidate();
        return dialog.ok;
    }

    private static void középretesz(Component component)
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int szélesség = component.getSize().width;
        int magasság = component.getSize().height;
        component.setSize(szélesség, magasság);
        component.setLocation((screenSize.width - szélesség) / 2, (screenSize.height - magasság) / 2);
    }

    private static Component addScrollableInBorders(Component component)
    {
        PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("3dlu,pref:grow,3dlu", "3dlu,pref:grow,3dlu"));
        panelBuilder.add(component, CC.xy(2, 2));
        JScrollPane scrollPane = new JScrollPane(panelBuilder.build());
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private static <INPUT> JFrame runInFrame(Component parent, IController<INPUT, ?> controller, INPUT input, JMenuBar jMenuBar, JToolBar toolBar, boolean fullScreen)
    {
        controller.initData(input);
        JXFrame frame = new JXFrame();
        frame.add(toolBar, BorderLayout.NORTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(controller.getView());
        frame.setTitle(controller.getTitle());
        frame.setName(controller.getTitle());
        frame.setJMenuBar(jMenuBar);
        controller.initBindings();
        frame.pack();
        középretesz(frame);
        if (fullScreen)
        {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        frame.setVisible(true);
        Component initFocusComponent = controller.getComponentForFocus();
        if (initFocusComponent != null)
        {
            initFocusComponent.requestFocusInWindow();
        }
        return frame;
    }

    private static Component getButtons(final MyDialog dialog, final IDialogController<?, ?> dialogController)
    {
        PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("pref:grow,3dlu,pref,3dlu,pref", "pref"));
        JButton okButton = new JButton(MENTÉS);
        okButton.setName(MENTÉS);
        JButton cancelButton = new JButton(MÉGSEM);
        cancelButton.setName(MÉGSEM);
        panelBuilder.add(okButton, CC.xy(3, 1));
        panelBuilder.add(cancelButton, CC.xy(5, 1));
        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dialogController.onOk();
                dialog.ok = true;
                dialog.dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dialogController.onCancel();
                dialog.dispose();
            }
        });
        return panelBuilder.build();
    }

    public static <INPUT> JFrame runInFrameFullScreen(IControllerMenüvel<INPUT, ?> controller, INPUT input)
    {
        return runInFrame(null, controller, input, controller.getMenuBar(), controller.getToolBar(), true);
    }
}
