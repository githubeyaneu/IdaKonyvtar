package eu.eyan.idakonyvtar.controller;

import static com.google.common.collect.Lists.newArrayList;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.fest.util.Objects;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.adapter.ComboBoxAdapter;

import eu.eyan.idakonyvtar.controller.input.KönyvControllerInput;
import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.model.KönyvMezőValueModel;
import eu.eyan.idakonyvtar.model.OszlopKonfiguráció.OszlopKonfigurációk;
import eu.eyan.idakonyvtar.oszk.Marc;
import eu.eyan.idakonyvtar.oszk.OszkKereso;
import eu.eyan.idakonyvtar.oszk.OszkKeresoException;
import eu.eyan.idakonyvtar.util.KönyvHelper;
import eu.eyan.idakonyvtar.view.KönyvView;
import eu.eyan.idakonyvtar.view.MultiMező;
import eu.eyan.idakonyvtar.view.MultiMezőJComboBox;
import eu.eyan.idakonyvtar.view.MultiMezőJTextField;

public class KönyvController implements IDialogController<KönyvControllerInput, Könyv>
{
    private KönyvView view = new KönyvView();
    private KönyvControllerInput model;
    private List<Window> resizeListeners = newArrayList();

    @Override
    public Component getView()
    {
        return view.getComponent();
    }

    @Override
    public String getTitle()
    {
        if (model.getOszlopok().indexOf("Szerző") >= 0)
        {
            return "KKönyv adatainak szerkesztése - " + model.getKönyv().getValue(model.getOszlopok().indexOf("Szerző"));
        }
        else
        {
            return "Könyv adatainak szerkesztése";
        }
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
        view.setOszlopKonfiguráció(model.getOszlopKonfiguráció());
    }

    @Override
    public void initBindings()
    {
        for (int oszlopIndex = 0; oszlopIndex < model.getOszlopok().size(); oszlopIndex++)
        {
            String oszlopNév = model.getOszlopok().get(oszlopIndex);
            boolean autoComplete = model.getOszlopKonfiguráció().isIgen(oszlopNév, OszlopKonfigurációk.AUTOCOMPLETE);
            boolean multi = model.getOszlopKonfiguráció().isIgen(oszlopNév, OszlopKonfigurációk.MULTIMEZŐ);
            if (autoComplete)
            {
                List<String> oszlopLista = KönyvHelper.getOszlopLista(model.getKönyvLista(), oszlopIndex);
                if (multi)
                {
                    MultiMezőJComboBox mmcombo = (MultiMezőJComboBox) view.getSzerkesztők().get(oszlopIndex);
                    mmcombo.setAutoCompleteLista(oszlopLista);
                    bind(mmcombo, new KönyvMezőValueModel(oszlopIndex, model.getKönyv()));
                }
                else
                {
                    JComboBox<?> comboBox = (JComboBox<?>) view.getSzerkesztők().get(oszlopIndex);
                    Bindings.bind(comboBox, new ComboBoxAdapter<String>(oszlopLista, new KönyvMezőValueModel(oszlopIndex, model.getKönyv())));
                    AutoCompleteDecorator.decorate(comboBox);
                }
            }
            else
            {
                if (multi)
                {
                    MultiMezőJTextField mmc = (MultiMezőJTextField) view.getSzerkesztők().get(oszlopIndex);
                    bind(mmc, new KönyvMezőValueModel(oszlopIndex, model.getKönyv()));
                }
                else
                {
                    Bindings.bind((JTextField) view.getSzerkesztők().get(oszlopIndex), new KönyvMezőValueModel(oszlopIndex, model.getKönyv()));
                }
            }
        }
        view.getIsbnText().addActionListener(isbnKeresés());
    }

    private void bind(final MultiMező<String, ?> mmc, final KönyvMezőValueModel könyvMezőValueModel)
    {
        könyvMezőValueModel.addValueChangeListener((PropertyChangeEvent propertyChangeEvent) -> {
            if (!Objects.areEqual(propertyChangeEvent.getNewValue(), propertyChangeEvent.getOldValue()))
            {
                mmc.setValues(getMultiMezőLista((String) propertyChangeEvent.getNewValue()));
            }
        });
        mmc.setValues(getMultiMezőLista((String) könyvMezőValueModel.getValue()));

        mmc.addPropertyChangeListener((PropertyChangeEvent propertyChangeEvent) -> {
            könyvMezőValueModel.setValue(Joiner.on(KönyvHelper.LISTA_SEPARATOR).skipNulls().join(mmc.getValues()));
        });
    }

    private static List<String> getMultiMezőLista(String value)
    {
        String[] strings = (value).split(KönyvHelper.LISTA_SEPARATOR_REGEX);
        List<String> lista = newArrayList(strings).stream().filter((String s) -> {
            return !s.isEmpty();
        }).collect(Collectors.toList());
        return lista;
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
                            fireResizeEvent();
                        }

                    });
                    fireResizeEvent();
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
                e.printStackTrace();
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

    @Override
    public Component getComponentForFocus()
    {
        return view.getIsbnText();
    }

    @Override
    public void addResizeListener(Window window)
    {
        this.resizeListeners.add(window);
    }

    private void fireResizeEvent()
    {
        for (Window window : resizeListeners)
        {
            window.pack();
        }
    }
}
