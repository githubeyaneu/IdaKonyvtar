package eu.eyan.idakonyvtar.view;

import static com.google.common.collect.Lists.newArrayList;

import java.awt.Component;
import java.util.List;

import javax.swing.JTextField;

import lombok.Getter;
import lombok.Setter;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class KönyvView implements IView
{
    @Setter
    private List<String> oszlopok = newArrayList();
    @Getter
    private List<JTextField> szerkesztők = newArrayList();

    @Override
    public Component getComponent()
    {
        String rowDef = "pref, 3dlu";
        String rowSpec = rowDef;
        for (int i = 1; i < oszlopok.size(); i++)
        {
            rowSpec += "," + rowDef;
        }
        PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("pref, pref:grow", rowSpec));
        for (int i = 0; i < oszlopok.size(); i++)
        {
            panelBuilder.addLabel(oszlopok.get(i), CC.xy(1, i * 2 + 1));
            // public JComboBox<String> valami = new JComboBox<String>();
            JTextField szerkesztő = new JTextField();
            szerkesztők.add(szerkesztő);
            panelBuilder.add(szerkesztő, CC.xy(2, i * 2 + 1));
        }
        return panelBuilder.build();
    }
}
