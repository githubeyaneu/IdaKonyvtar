package eu.eyan.idakonyvtar.controller;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.google.common.io.Resources;
import com.jgoodies.binding.adapter.Bindings;

import eu.eyan.idakonyvtar.controller.input.KönyvControllerInput;
import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.model.KönyvMezőValueModel;
import eu.eyan.idakonyvtar.oszk.Marc;
import eu.eyan.idakonyvtar.oszk.MarcCodes;
import eu.eyan.idakonyvtar.oszk.MarcHelper;
import eu.eyan.idakonyvtar.oszk.OszkKereso;
import eu.eyan.idakonyvtar.oszk.OszkKeresoException;
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
        return new Dimension(400, 600);
    }

    @Override
    public void initData(KönyvControllerInput model)
    {
        this.model = model;
        view.setOszlopok(model.getOszlopok());
        view.setIsbnEnabled(model.isIsbnEnabled());
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
        view.getIsbnText().addActionListener(isbnKeresés());
    }

    private ActionListener isbnKeresés()
    {
        return new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (e.getSource() == view.getIsbnText())
                {
                    System.out.println("ISBN Action: " + view.getIsbnText().getText());
                    view.getIsbnText().selectAll();
                    view.getIsbnKeresőLabel().setText("Keresés");
                    view.getIsbnKeresőLabel().setIcon(new ImageIcon(Resources.getResource("icons/keresés.gif")));
                    // TODO Asynchron
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            List<Marc> marcsToIsbn;
                            try
                            {
                                marcsToIsbn = OszkKereso.getMarcsToIsbn(view.getIsbnText().getText().replaceAll("ö", "0"));
                                String cim = MarcHelper.findMarc(marcsToIsbn, MarcCodes.CIM);
                                view.getIsbnKeresőLabel().setText("Cím: " + cim);
                                view.getIsbnKeresőLabel().setIcon(null);
                                for (Marc marc : marcsToIsbn)
                                {
                                    System.out.println(marc.getValue());
//                                    view.result.setText(view.result.getText() + "<br>\r\n"
//                                            + Joiner.on(" - ").join(marc.getMarc1(), marc.getMarc2(), marc.getMarc3(), marc.getValue()).toString());
                                }
                                System.out.println("Talált: " + cim);
                            }
                            catch (OszkKeresoException e)
                            {
                                // FIXME: itt fontos a naplózás
//                                e.printStackTrace();
                                view.getIsbnKeresőLabel().setText("Nincs találat");
                                view.getIsbnKeresőLabel().setIcon(new ImageIcon(Resources.getResource("icons/hiba.gif")));
                            }
                        }
                    });

                }

            }
        };
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
