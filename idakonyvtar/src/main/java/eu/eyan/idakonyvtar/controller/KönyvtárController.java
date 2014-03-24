package eu.eyan.idakonyvtar.controller;

import static eu.eyan.idakonyvtar.controller.input.KönyvControllerInput.ISBN_ENABLED;
import static javax.swing.JFileChooser.APPROVE_OPTION;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jgoodies.binding.adapter.SingleListSelectionAdapter;

import eu.eyan.idakonyvtar.controller.adapter.KönyvtárListaTableModel;
import eu.eyan.idakonyvtar.controller.input.KönyvControllerInput;
import eu.eyan.idakonyvtar.controller.input.KönyvtárControllerInput;
import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.model.KönyvtárModel;
import eu.eyan.idakonyvtar.util.DialogHelper;
import eu.eyan.idakonyvtar.util.ExcelKezelő;
import eu.eyan.idakonyvtar.util.KiemelőRenderer;
import eu.eyan.idakonyvtar.util.MagyarRowFilter;
import eu.eyan.idakonyvtar.util.OkCancelDialog;
import eu.eyan.idakonyvtar.view.KönyvtárMenüAndToolBar;
import eu.eyan.idakonyvtar.view.KönyvtárView;

public class KönyvtárController implements IControllerMenüvel<KönyvtárControllerInput, Void>, ActionListener
{

    public static final String MEGERŐSÍTÉS_NEM = "Nem";
    public static final String MEGERŐSÍTÉS_IGEN = "Igen";
    public static final String TITLE = "Ida könyvtára";
    private final KönyvtárMenüAndToolBar menuAndToolBar = new KönyvtárMenüAndToolBar();
    private final KönyvtárView view = new KönyvtárView();
    private final KönyvtárModel model = new KönyvtárModel();
    private Könyv emlékKönyv;
    KiemelőRenderer kiemelőRenderer = new KiemelőRenderer();

    // FIXME: initview and getview are not the same!!
    @Override
    public Component getView()
    {
        view.getComponent();
        resetTableModel();
        view.getKönyvTábla().setSelectionModel(new SingleListSelectionAdapter(model.getKönyvek().getSelectionIndexHolder()));
        view.getKönyvTábla().setEnabled(true);
        view.getKönyvTábla().setDefaultRenderer(Object.class, kiemelőRenderer);
        view.getKönyvTábla().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return view.getComponent();
    }

    private void resetTableModel()
    {
        // FIXME: tényleg szükség van erre????
        if (model.getKönyvek().getSize() > 0)
        {
            view.getKönyvTábla().setÜresSzöveg("Ilyen szűrőfeltételekkel nem található könyv.");
        }
        else
        {
            view.getKönyvTábla().setÜresSzöveg("Nincs könyv a listában.");
        }
        KönyvtárListaTableModel dataModel = new KönyvtárListaTableModel(model.getKönyvek(), model.getKönyvtár().getOszlopok(), model.getKönyvtár().getKonfiguráció());
        view.getKönyvTábla().setModel(dataModel);
    }

    @Override
    public String getTitle()
    {
        return TITLE;
    }

    @Override
    public void initData(KönyvtárControllerInput input)
    {
        readKönyvtár(input.getFile());
        emlékKönyv = new Könyv(model.getKönyvtár().getOszlopok().size());
    }

    private void readKönyvtár(File file)
    {
        System.out.println("Fájl betöltése: " + file);
        model.setKönyvtár(ExcelKezelő.könyvtárBeolvasása(file));
        model.getKönyvek().getList().clear();
        // FIXME heee? 2x a modelben
        model.getKönyvek().setList(model.getKönyvtár().getKönyvek());
        resetTableModel();
    }

    private void saveKönyvtár(File file)
    {
        ExcelKezelő.könyvtárMentése(file, model.getKönyvtár());
    }

    @Override
    public void initBindings()
    {
        menuAndToolBar.MENÜPONT_EXCEL_TÖLTÉS.addActionListener(this);
        menuAndToolBar.MENÜPONT_EXCEL_MENTÉS.addActionListener(this);

        menuAndToolBar.TOOLBAR_UJ_KONYV.addActionListener(this);
        menuAndToolBar.TOOLBAR_KONYV_TOROL.addActionListener(this);

        view.getKönyvTábla().getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                menuAndToolBar.TOOLBAR_KONYV_TOROL.setEnabled(view.getKönyvTábla().getSelectedRow() >= 0);
            }
        });

        view.getKönyvTábla().addMouseListener(new MouseAdapter()
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

        menuAndToolBar.TOOLBAR_KERES.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                view.getKönyvTábla().setRowFilter(new MagyarRowFilter(menuAndToolBar.TOOLBAR_KERES.getText()));
                kiemelőRenderer.setKiemelendőSzöveg(menuAndToolBar.TOOLBAR_KERES.getText());
            }
        });

        JFrame frame = (JFrame) SwingUtilities.getRoot(view.getComponent());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (DialogHelper.igenNem(frame, "Biztos ki akar lépni?", "Megerősítés"))
                {
                    frame.dispose();
                }
            }
        });
    }

    private void könyvSzerkesztés()
    {
        KönyvController könyvController = new KönyvController();
        int selectedKönyvIndex = view.getKönyvTábla().convertRowIndexToModel(view.getKönyvTábla().getSelectedRow());
        OkCancelDialog szerkesztőDialog = DialogHelper.startModalDialog(
                view.getComponent(),
                könyvController,
                new KönyvControllerInput.Builder()
                        .withKönyv(new Könyv(model.getKönyvek().getList().get(selectedKönyvIndex)))
                        .withOszlopok(model.getKönyvtár().getOszlopok())
                        .withOszlopKonfiguráció(model.getKönyvtár().getKonfiguráció())
                        .withKönyvLista(model.getKönyvtár().getKönyvek())
                        .build());
        if (szerkesztőDialog.isOk())
        {
            model.getKönyvek().getList().set(selectedKönyvIndex, könyvController.getOutput());
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

        if (e.getSource() == menuAndToolBar.TOOLBAR_UJ_KONYV)
        {
            KönyvController könyvController = new KönyvController();

            OkCancelDialog szerkesztőDialog = DialogHelper.startModalDialog(
                    view.getComponent()
                    , könyvController
                    , new KönyvControllerInput.Builder()
                            .withKönyv(újKönyvEmlékekkel(model.getKönyvtár().getOszlopok().size()))
                            .withKönyvLista(model.getKönyvek().getList())
                            .withOszlopok(model.getKönyvtár().getOszlopok())
                            .withIsbnEnabled(ISBN_ENABLED)
                            .withOszlopKonfiguráció(model.getKönyvtár().getKonfiguráció())
                            .build());
            if (szerkesztőDialog.isOk())
            {
                model.getKönyvek().getList().add(0, könyvController.getOutput());
                emlékekMentése(könyvController.getOutput());
                // TODO: ugly: use selectioninlist...
                model.getKönyvek().fireIntervalAdded(0, 0);
            }
        }

        if (e.getSource() == menuAndToolBar.TOOLBAR_KONYV_TOROL)
        {
            if (JOptionPane.showOptionDialog(menuAndToolBar.TOOLBAR_KONYV_TOROL, "Biztosan törölni akarod?", "Törlés megerősítése", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] { MEGERŐSÍTÉS_IGEN, MEGERŐSÍTÉS_NEM }, MEGERŐSÍTÉS_NEM) == JOptionPane.OK_OPTION)
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

    @Override
    public Component getComponentForFocus()
    {
        return menuAndToolBar.TOOLBAR_KERES;
    }
}