package eu.eyan.idakonyvtar.view;

import static com.google.common.collect.Lists.newArrayList;

import java.awt.Component;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import lombok.Getter;
import lombok.Setter;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import eu.eyan.idakonyvtar.model.OszlopKonfiguráció;
import eu.eyan.idakonyvtar.model.OszlopKonfiguráció.OszlopKonfigurációk;

public class KönyvView implements IView
{
    public static final String ISBN_TEXT = "isbnText";

    public static final String ISBN_LABEL = "isbnLabel";

    @Setter
    private List<String> oszlopok = newArrayList();

    @Setter
    private boolean isbnEnabled = false;

    @Getter
    private List<Component> szerkesztők = newArrayList();

    @Getter
    private JLabel isbnKeresőLabel = new JLabel();

    @Getter
    private JTextField isbnText = new JTextField();

    @Setter
    private OszlopKonfiguráció oszlopKonfiguráció;

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
            isbnKeresőLabel.setName(ISBN_LABEL);
            panelBuilder.add(isbnText, CC.xyw(3, row, 1));
            isbnText.setName(ISBN_TEXT);
            row = row + 2;
        }
        panelBuilder.addSeparator("Adatok", CC.xyw(1, row, 3));
        for (int i = 0; i < oszlopok.size(); i++)
        {
            row = row + 2;
            String oszlopNév = oszlopok.get(i);
            panelBuilder.addLabel(oszlopNév, CC.xy(1, row));

            Component szerkesztő;
            if (oszlopKonfiguráció.isIgen(oszlopNév, OszlopKonfigurációk.AUTOCOMPLETE))
            {
                JComboBox<String> jComboBox = new JComboBox<String>();
                jComboBox.setEditable(true);
                szerkesztő = jComboBox;
            }
            else
            {
                szerkesztő = new JTextField(20);
            }
            szerkesztő.setName(oszlopNév);
            szerkesztők.add(szerkesztő);
            panelBuilder.add(szerkesztő, CC.xy(3, row));
        }
        return panelBuilder.build();
    }

}
