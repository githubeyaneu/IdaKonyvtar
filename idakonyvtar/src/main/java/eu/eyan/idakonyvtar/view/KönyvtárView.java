package eu.eyan.idakonyvtar.view;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import lombok.Getter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class KönyvtárView
{
    @Getter
    private final KönyvTábla könyvTábla = new KönyvTábla();

    private JScrollPane scrollPane;

    public Component getComponent()
    {
        if (scrollPane == null)
        {
            JPanel panel = new JPanel(new FormLayout("pref:grow", "pref, pref:grow"));
            scrollPane = new JScrollPane(könyvTábla);
            panel.add(scrollPane, CC.xy(1, 2));
        }
        return scrollPane;
    }
}
