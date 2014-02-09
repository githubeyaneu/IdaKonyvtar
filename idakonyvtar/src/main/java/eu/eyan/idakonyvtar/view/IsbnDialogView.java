package eu.eyan.idakonyvtar.view;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class IsbnDialogView implements IView
{
    public JLabel isbnLabel = new JLabel("Isbn");
    public JTextField isbnText = new JTextField();
    public JLabel resultLabel = new JLabel("Result");
    public JLabel result = new JLabel("-");
    private JPanel panel;

    public Component getComponent()
    {
        if (panel == null)
        {
            panel = new JPanel(new FormLayout("pref, 3dlu, 200px:grow", "pref, pref:grow"));
            isbnText.requestFocusInWindow();
            panel.add(isbnLabel, CC.xy(1, 1));
            panel.add(isbnText, CC.xy(3, 1));
            panel.add(resultLabel, CC.xy(1, 2));
            panel.add(result, CC.xy(3, 2));
        }
        return panel;
    }

}
