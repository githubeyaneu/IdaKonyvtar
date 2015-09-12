package eu.eyan.idakonyvtar.view;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import lombok.Getter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class LibraryView extends AbstractView
{
    @Getter
    private final BookTable bookTable = new BookTable();

    @Override
    public Component createViewComponent()
    {
        JPanel panel = new JPanel(new FormLayout("pref:grow", "pref, pref:grow"));
        JScrollPane scrollPane = new JScrollPane(bookTable);
        panel.add(scrollPane, CC.xy(1, 2));
        return scrollPane;
    }
}