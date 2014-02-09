package eu.eyan.idakonyvtar.view;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class KönyvView implements IView
{
    public JTextField cim = new JTextField();
    public JTextField szerzo = new JTextField();
    public JComboBox<String> valami = new JComboBox<String>();

    @Override
    public Component getComponent()
    {
        PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("pref, pref:grow", "pref, pref, pref"));
        panelBuilder.addLabel("Cím", CC.xy(1, 1));
        panelBuilder.addLabel("Szerző", CC.xy(1, 2));
        panelBuilder.addLabel("Str...", CC.xy(1, 3));

        panelBuilder.add(cim, CC.xy(2, 1));
        panelBuilder.add(szerzo, CC.xy(2, 2));
        panelBuilder.add(valami, CC.xy(2, 3));

        return panelBuilder.build();
    }

}
