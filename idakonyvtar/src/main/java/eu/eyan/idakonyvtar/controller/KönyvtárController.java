package eu.eyan.idakonyvtar.controller;

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
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jgoodies.binding.adapter.SingleListSelectionAdapter;

import eu.eyan.idakonyvtar.controller.adapter.KönyvtárListaTableModel;
import eu.eyan.idakonyvtar.controller.input.KönyvControllerInput;
import eu.eyan.idakonyvtar.controller.input.KönyvtárControllerInput;
import eu.eyan.idakonyvtar.data.ExcelKezelő;
import eu.eyan.idakonyvtar.menu.IdaKönyvtárMenü;
import eu.eyan.idakonyvtar.model.IdaKönyvtárModel;
import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.util.DialogHandler;
import eu.eyan.idakonyvtar.view.IdaKönyvtárView;

public class KönyvtárController implements IControllerMenüvel<KönyvtárControllerInput, Void>, ActionListener
{

    private final IdaKönyvtárMenü menu = new IdaKönyvtárMenü();
    private final IdaKönyvtárView view = new IdaKönyvtárView();
    private final IdaKönyvtárModel model = new IdaKönyvtárModel();
    // FIXME:
    private KönyvtárListaTableModel dataModel;

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
    }

    private void readKönyvtár(File file)
    {
        model.setKönyvtár(ExcelKezelő.könyvtárBeolvasása(file));
        model.getKönyvek().getList().clear();
        // FIXME heee? 2x a modelben
        model.getKönyvek().getList().addAll(model.getKönyvtár().getKönyvek());
    }

    private void saveKönyvtár(File file)
    {
        ExcelKezelő.könyvtárMentése(file, model.getKönyvtár());
    }

    @Override
    public void initDataBindings()
    {
        menu.EXCEL_TÖLTÉS.addActionListener(this);
        menu.ISBN_KERES.addActionListener(this);
        menu.EXCEL_MENTÉS.addActionListener(this);

        view.könyvTábla.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    KönyvController könyvController = new KönyvController();
                    if (DialogHandler.startModalDialog(view.getComponent(), könyvController, new KönyvControllerInput(new Könyv(dataModel.getSelectedKönyv()), model.getKönyvek().getList(), model.getKönyvtár().getOszlopok())))
                    {
                        model.getKönyvek().getList().set(model.getKönyvek().getSelectionIndex(), könyvController.getOutput());
                    }
//                    DialogHandler.runInModalerFrame(view.getComponent(), new KönyvController(), new KönyvControllerInput(dataModel.getSelectedKönyv(), model.getKönyvek().getList()));
                }
            }
        });
    }

    @Override
    public JMenuBar getMenuBar()
    {
        return menu;
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        if (e.getSource() == menu.EXCEL_TÖLTÉS)
        {
            JFileChooser jFileChooser = new JFileChooser(".");
            jFileChooser.setApproveButtonText("Töltés");
            jFileChooser.setFileFilter(new FileNameExtensionFilter("Excel97 fájlok", "xls"));
            if (jFileChooser.showOpenDialog(menu.EXCEL_TÖLTÉS) == APPROVE_OPTION)
            {
                readKönyvtár(jFileChooser.getSelectedFile());
            }
        }

        if (e.getSource() == menu.EXCEL_MENTÉS)
        {
            JFileChooser jFileChooser = new JFileChooser(new File("."));
            jFileChooser.setApproveButtonText("Mentés");
            jFileChooser.setFileFilter(new FileNameExtensionFilter("Excel97 fájlok", "xls"));
            if (jFileChooser.showOpenDialog(menu.EXCEL_MENTÉS) == APPROVE_OPTION)
            {
                System.out.println("Save " + jFileChooser.getSelectedFile());
                saveKönyvtár(jFileChooser.getSelectedFile());
            }
        }

        if (e.getSource() == menu.ISBN_KERES)
        {
            DialogHandler.startModalDialog(menu.ISBN_KERES, new IsbnController(), null);
        }
    }

    @Override
    public Void getOutput()
    {
        return null;
    }
}