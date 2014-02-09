package eu.eyan.idakonyvtar.controller;

import static eu.eyan.idakonyvtar.util.KönyvHelper.getMindenKiadó;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.adapter.ComboBoxAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;

import eu.eyan.idakonyvtar.controller.input.KönyvControllerInput;
import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.view.KönyvView;

public class KönyvController implements IController<KönyvControllerInput>
{
    private KönyvView view = new KönyvView();
    private KönyvControllerInput model;

    @Override
    public Component getView()
    {
        return view.getComponent();
    }

    @Override
    public String getTitle()
    {
        return "Könyv adatainak szerkesztése - " + model.könyv.getCim();
    }

    @Override
    public Dimension getDefaultSize()
    {
        return new Dimension(300, 300);
    }

    @Override
    public void initData(KönyvControllerInput model)
    {
        this.model = model;
    }

    @Override
    public void initDataBindings()
    {
        Bindings.bind(view.cim, new PropertyAdapter<Könyv>(model.könyv, "cim"));

        Bindings.bind(view.szerzo, new PropertyAdapter<Könyv>(model.könyv, "szerző"));

        view.valami.setEditable(true);
        Bindings.bind(view.valami, new ComboBoxAdapter<String>(getMindenKiadó(model.könyvLista), new PropertyAdapter<Könyv>(model.könyv, "kiadó")));
        // Disgusting Hack but works...
        AutoCompleteDecorator.decorate(view.valami);
        view.valami.addActionListener(new ActionListener()
        {
            private String last;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                String selectedItem = (String) view.valami.getSelectedItem();
                if (selectedItem != null)
                {
                    last = selectedItem;
                }
                System.out.println("Last " + last);
                model.könyv.setKiadó(last);

            }
        });
    }
}
