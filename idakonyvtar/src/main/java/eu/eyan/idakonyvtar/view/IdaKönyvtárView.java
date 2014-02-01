package eu.eyan.idakonyvtar.view;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class IdaKönyvtárView
{
    public final JTable könyvTábla = new JTable();

    public Component getComponent()
    {
        JPanel panel = new JPanel(new FormLayout("pref:grow", "pref:grow"));
        JScrollPane jScrollPane = new JScrollPane(könyvTábla);
        panel.add(jScrollPane, CC.xy(1, 1));
        return jScrollPane;
    }
}
