package eu.eyan.idakonyvtar.view;

import static com.google.common.collect.Lists.newArrayList;

import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
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
    @Setter
    private boolean isbnEnabled = false;

    @Getter
    private List<JTextField> szerkesztők = newArrayList();

    @Getter
    private JLabel isbnKeresőLabel = new JLabel();

    @Getter
    private JTextField isbnText = new JTextField();

    @Override
    public Component getComponent()
    {
        String rowSpec = "";
        if (isbnEnabled)
        {
            rowSpec += "pref, 3dlu, pref, 3dlu, ";
        }
        rowSpec += rowSpec + "pref";
        for (int i = 0; i < oszlopok.size(); i++)
        {
            rowSpec += ",3dlu ,pref";
        }
        PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("pref, 3dlu, pref:grow", rowSpec));

        int row = 1;
        if (isbnEnabled)
        {
            panelBuilder.addSeparator("Isbn", CC.xyw(1, row, 3));
            row = row + 2;
            panelBuilder.add(isbnKeresőLabel, CC.xyw(1, row, 1));
            panelBuilder.add(isbnText, CC.xyw(3, row, 1));
            row = row + 2;
        }
        panelBuilder.addSeparator("Adatok", CC.xyw(1, row, 3));
        for (int i = 0; i < oszlopok.size(); i++)
        {
            row = row + 2;
            panelBuilder.addLabel(oszlopok.get(i), CC.xy(1, row));
            // public JComboBox<String> valami = new JComboBox<String>();
            JTextField szerkesztő = new JTextField(20);
            szerkesztők.add(szerkesztő);
            panelBuilder.add(szerkesztő, CC.xy(3, row));
        }
        return panelBuilder.build();
    }
}
