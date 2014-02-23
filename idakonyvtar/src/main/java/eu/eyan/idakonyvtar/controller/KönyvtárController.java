package eu.eyan.idakonyvtar.controller;

import static eu.eyan.idakonyvtar.controller.input.KönyvControllerInput.ISBN_ENABLED;
import static javax.swing.JFileChooser.APPROVE_OPTION;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jgoodies.binding.adapter.SingleListSelectionAdapter;

import eu.eyan.idakonyvtar.controller.adapter.KönyvtárListaTableModel;
import eu.eyan.idakonyvtar.controller.input.KönyvControllerInput;
import eu.eyan.idakonyvtar.controller.input.KönyvtárControllerInput;
import eu.eyan.idakonyvtar.data.ExcelKezelő;
import eu.eyan.idakonyvtar.menu.IdaKönyvtárMenüAndToolBar;
import eu.eyan.idakonyvtar.model.IdaKönyvtárModel;
import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.util.DialogHandler;
import eu.eyan.idakonyvtar.view.IdaKönyvtárView;

public class KönyvtárController implements IControllerMenüvel<KönyvtárControllerInput, Void>, ActionListener
{

    private final IdaKönyvtárMenüAndToolBar menuAndToolBar = new IdaKönyvtárMenüAndToolBar();
    private final IdaKönyvtárView view = new IdaKönyvtárView();
    private final IdaKönyvtárModel model = new IdaKönyvtárModel();
    // FIXME: tényleg szükség van erre????
    private KönyvtárListaTableModel dataModel;
    private Könyv emlékKönyv;

    // FIXME: initview and getview are not the same!!
    @Override
    public Component getView()
    {
        view.getComponent();
        dataModel = new KönyvtárListaTableModel(model.getKönyvek(), model.getKönyvtár().getOszlopok());
        view.könyvTábla.setModel(dataModel);
        view.könyvTábla.setSelectionModel(new SingleListSelectionAdapter(model.getKönyvek().getSelectionIndexHolder()));
        view.könyvTábla.setEnabled(true);
        return view.getComponent();
    }

    @Override
    public String getTitle()
    {
        return "Ida könyvtára";
    }

    @Override
    public Dimension getDefaultSize()
    {
        return new Dimension(1000, 600);
    }

    @Override
    public void initData(KönyvtárControllerInput input)
    {
        readKönyvtár(input.getFile());
        emlékKönyv = new Könyv(model.getKönyvtár().getOszlopok().size());
    }

    private void readKönyvtár(File file)
    {
        model.setKönyvtár(ExcelKezelő.könyvtárBeolvasása(file));
        model.getKönyvek().getList().clear();
        // FIXME heee? 2x a modelben
        model.getKönyvek().setList(model.getKönyvtár().getKönyvek());
    }

    private void saveKönyvtár(File file)
    {
        ExcelKezelő.könyvtárMentése(file, model.getKönyvtár());
    }

    @Override
    public void initDataBindings()
    {
        menuAndToolBar.MENÜPONT_EXCEL_TÖLTÉS.addActionListener(this);
        menuAndToolBar.MENÜPONT_ISBN_KERES.addActionListener(this);
        menuAndToolBar.MENÜPONT_EXCEL_MENTÉS.addActionListener(this);

        menuAndToolBar.TOOLBAR_UJ_KONYV.addActionListener(this);
        menuAndToolBar.TOOLBAR_KONYV_TOROL.addActionListener(this);

        view.könyvTábla.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                menuAndToolBar.TOOLBAR_KONYV_TOROL.setEnabled(e.getFirstIndex() >= 0);
            }
        });

        view.könyvTábla.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    könyvSzerkesztés();
                }
            }

        });
    }

    private void könyvSzerkesztés()
    {
        KönyvController könyvController = new KönyvController();
        if (DialogHandler.startModalDialog(view.getComponent(), könyvController, new KönyvControllerInput(new Könyv(dataModel.getSelectedKönyv()), model.getKönyvek().getList(), model.getKönyvtár().getOszlopok())))
        {
            model.getKönyvek().getList().set(model.getKönyvek().getSelectionIndex(), könyvController.getOutput());
            // TODO: ugly: use selectioninlist...
            model.getKönyvek().fireSelectedContentsChanged();
        }
    }

    @Override
    public JMenuBar getMenuBar()
    {
        return menuAndToolBar.getMenuBar();
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        if (e.getSource() == menuAndToolBar.MENÜPONT_EXCEL_TÖLTÉS)
        {
            JFileChooser jFileChooser = new JFileChooser(".");
            jFileChooser.setApproveButtonText("Töltés");
            jFileChooser.setFileFilter(new FileNameExtensionFilter("Excel97 fájlok", "xls"));
            if (jFileChooser.showOpenDialog(menuAndToolBar.MENÜPONT_EXCEL_TÖLTÉS) == APPROVE_OPTION)
            {
                readKönyvtár(jFileChooser.getSelectedFile());
            }
        }

        if (e.getSource() == menuAndToolBar.MENÜPONT_EXCEL_MENTÉS)
        {
            JFileChooser jFileChooser = new JFileChooser(new File("."));
            jFileChooser.setApproveButtonText("Mentés");
            jFileChooser.setFileFilter(new FileNameExtensionFilter("Excel97 fájlok", "xls"));
            if (jFileChooser.showOpenDialog(menuAndToolBar.MENÜPONT_EXCEL_MENTÉS) == APPROVE_OPTION)
            {
                System.out.println("Save " + jFileChooser.getSelectedFile());
                saveKönyvtár(jFileChooser.getSelectedFile());
            }
        }

        if (e.getSource() == menuAndToolBar.MENÜPONT_ISBN_KERES || e.getSource() == menuAndToolBar.TOOLBAR_UJ_KONYV)
        {
            KönyvController könyvController = new KönyvController();
            if (DialogHandler.startModalDialog(
                    view.getComponent()
                    , könyvController
                    , new KönyvControllerInput(
                            újKönyvEmlékekkel(model.getKönyvtár().getOszlopok().size())
                            , model.getKönyvek().getList()
                            , model.getKönyvtár().getOszlopok()
                            , ISBN_ENABLED
                            , model.getKönyvtár().getKonfiguráció())))
            {
                model.getKönyvek().getList().add(0, könyvController.getOutput());
                emlékekMentése(könyvController.getOutput());
                // TODO: ugly: use selectioninlist...
                model.getKönyvek().fireIntervalAdded(0, 0);
            }
        }

        if (e.getSource() == menuAndToolBar.TOOLBAR_KONYV_TOROL)
        {
            if (JOptionPane.showOptionDialog(menuAndToolBar.TOOLBAR_KONYV_TOROL, "Biztosan törölni akarod?", "Törlés megerősítése", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] { "Igen", "Nem" }, "Nem") == JOptionPane.OK_OPTION)
            {
                int selectionIndex = model.getKönyvek().getSelectionIndex();
                model.getKönyvek().getList().remove(selectionIndex);
                // TODO: ugly: use selectioninlist...
                model.getKönyvek().fireIntervalRemoved(selectionIndex, selectionIndex);
            }
        }
    }

    private void emlékekMentése(Könyv könyv)
    {
        emlékKönyv = new Könyv(model.getKönyvtár().getOszlopok().size());
        for (String emlékezőOszlop : model.getKönyvtár().getKonfiguráció().getEmlékezőOszlopok())
        {
            int oszlopIndex = model.getKönyvtár().getOszlopok().indexOf(emlékezőOszlop);
            emlékKönyv.setValue(oszlopIndex, könyv.getValue(oszlopIndex));
        }
    }

    private Könyv újKönyvEmlékekkel(int size)
    {
        Könyv újKönyv = new Könyv(size);
        for (String emlékezőOszlop : model.getKönyvtár().getKonfiguráció().getEmlékezőOszlopok())
        {
            int oszlopIndex = model.getKönyvtár().getOszlopok().indexOf(emlékezőOszlop);
            újKönyv.setValue(oszlopIndex, emlékKönyv.getValue(oszlopIndex));
        }
        return újKönyv;
    }

    @Override
    public Void getOutput()
    {
        return null;
    }

    @Override
    public JToolBar getToolBar()
    {
        return menuAndToolBar.getToolBar();
    }
}