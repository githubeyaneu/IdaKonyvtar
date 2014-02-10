package eu.eyan.idakonyvtar.controller;

import static javax.swing.JFileChooser.APPROVE_OPTION;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;

import com.jgoodies.binding.adapter.SingleListSelectionAdapter;

import eu.eyan.idakonyvtar.controller.adapter.KönyvtárListaTableModel;
import eu.eyan.idakonyvtar.controller.input.KönyvControllerInput;
import eu.eyan.idakonyvtar.controller.input.KönyvtárControllerInput;
import eu.eyan.idakonyvtar.data.ExcelKezelő;
import eu.eyan.idakonyvtar.menu.IdaKönyvtárMenü;
import eu.eyan.idakonyvtar.model.IdaKönyvtárModel;
import eu.eyan.idakonyvtar.util.DialogHandler;
import eu.eyan.idakonyvtar.view.IdaKönyvtárView;

public class KönyvtárController implements IControllerMenüvel<KönyvtárControllerInput>, ActionListener
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
        dataModel = new KönyvtárListaTableModel(model.getKönyvek());
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
        model.getKönyvek().getList().clear();
        model.getKönyvek().getList().addAll(ExcelKezelő.könyvtárBeolvasása(input.getFile()));
    }

    @Override
    public void initDataBindings()
    {
        menu.IMPORT_EXCEL.addActionListener(this);
        menu.ISBN_KERES.addActionListener(this);

        view.könyvTábla.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    DialogHandler.startModalDialog(view.getComponent(), new KönyvController(), new KönyvControllerInput(dataModel.getSelectedKönyv(), model.getKönyvek().getList()));
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
        if (e.getSource() == menu.IMPORT_EXCEL)
        {
            JFileChooser jFileChooser = new JFileChooser();
            if (jFileChooser.showOpenDialog(menu.IMPORT_EXCEL) == APPROVE_OPTION)
            {
                model.getKönyvek().getList().clear();
                model.getKönyvek().setList(ExcelKezelő.könyvtárBeolvasása(jFileChooser.getSelectedFile()));
            }
        }

        if (e.getSource() == menu.ISBN_KERES)
        {
            DialogHandler.startModalDialog(menu.ISBN_KERES, new IsbnController(), null);
        }
    }
}