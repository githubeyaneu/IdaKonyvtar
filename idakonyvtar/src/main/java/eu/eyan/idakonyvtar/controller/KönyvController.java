package eu.eyan.idakonyvtar.controller;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.common.io.Resources;
import com.jgoodies.binding.adapter.Bindings;

import eu.eyan.idakonyvtar.controller.input.KönyvControllerInput;
import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.model.KönyvMezőValueModel;
import eu.eyan.idakonyvtar.oszk.Marc;
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
    public void initBindings()
    {
        for (int i = 0; i < model.getOszlopok().size(); i++)
        {
            Bindings.bind(view.getSzerkesztők().get(i), new KönyvMezőValueModel(i, model.getKönyv()));
        }

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
                                isbnAdatokBedolgozása(marcsToIsbn);
                            }
                            catch (OszkKeresoException e)
                            {
                                // FIXME: itt fontos a naplózás
                                view.getIsbnKeresőLabel().setText("Nincs találat");
                                view.getIsbnKeresőLabel().setIcon(new ImageIcon(Resources.getResource("icons/hiba.gif")));
                            }
                        }

                    });

                }

            }
        };
    }

    private void isbnAdatokBedolgozása(List<Marc> marcsToIsbn)
    {
        for (String oszlop : model.getOszlopok())
        {
            String oszlopÉrték = "";
            List<Marc> oszlophozRendeltMarcKódok;
            try
            {
                oszlophozRendeltMarcKódok = model.getOszlopKonfiguráció().getMarcKódok(oszlop);
                for (Marc marc : marcsToIsbn)
                {
                    for (Marc oszlopMarc : oszlophozRendeltMarcKódok)
                    {
                        if (marcStimmel(marc, oszlopMarc))
                        {
                            oszlopÉrték += oszlopÉrték.equals("") ? marc.getValue() : ", " + marc.getValue();
                        }
                    }
                }
                model.getKönyv().setValue(model.getOszlopok().indexOf(oszlop), oszlopÉrték);
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
            }
        }
    }

    private boolean marcStimmel(Marc pontosMarc, Marc pontatlanMarc)
    {
        if (pontosMarc == null || pontatlanMarc == null || pontosMarc.getMarc1() == null || pontatlanMarc.getMarc1() == null)
        {
            return false;
        }
        if (pontosMarc.getMarc1().equalsIgnoreCase(pontatlanMarc.getMarc1()))
        {
            if (pontatlanMarc.getMarc2().equals("") || pontosMarc.getMarc2().equalsIgnoreCase(pontatlanMarc.getMarc2()))
            {
                if (pontatlanMarc.getMarc3().equals("") || pontosMarc.getMarc3().equalsIgnoreCase(pontatlanMarc.getMarc3()))
                {
                    return true;
                }
            }
        }
        return false;
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
