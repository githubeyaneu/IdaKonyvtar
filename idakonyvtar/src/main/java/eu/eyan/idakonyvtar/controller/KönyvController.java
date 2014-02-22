package eu.eyan.idakonyvtar.controller;

import java.awt.Component;
import java.awt.Dimension;

import com.jgoodies.binding.adapter.Bindings;

import eu.eyan.idakonyvtar.controller.input.KönyvControllerInput;
import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.model.KönyvMezőValueModel;
import eu.eyan.idakonyvtar.view.KönyvView;

public class KönyvController implements IDialogController<KönyvControllerInput, Könyv>
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
        return "Könyv adatainak szerkesztése - " + model.getKönyv().getValue(model.getOszlopok().indexOf("Szerző"));
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
        view.setOszlopok(model.getOszlopok());
    }

    @Override
    public void initDataBindings()
    {
        for (int i = 0; i < model.getOszlopok().size(); i++)
        {
            Bindings.bind(view.getSzerkesztők().get(i), new KönyvMezőValueModel(i, model.getKönyv()));
        }

//        Bindings.bind(view.cim, new PropertyAdapter<Könyv>(model.getKönyv(), "cim"));

//        Bindings.bind(view.szerzo, new PropertyAdapter<Könyv>(model.getKönyv(), "szerző"));

//        view.valami.setEditable(true);
//        Bindings.bind(view.valami, new ComboBoxAdapter<String>(getMindenKiadó(model.getKönyvLista()), new PropertyAdapter<Könyv>(model.getKönyv(), "kiadó")));
//        AutoCompleteDecorator.decorate(view.valami);
    }

    @Override
    public void onOk()
    {
    }

    @Override
    public void onCancel()
    {
    }

    @Override
    public Könyv getOutput()
    {
        return model.getKönyv();
    }
}
