package eu.eyan.idakonyvtar.menu;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import lombok.Getter;

import com.google.common.io.Resources;

public class IdaKönyvtárMenüAndToolBar
{
    public final JMenuItem MENÜPONT_EXCEL_TÖLTÉS = new JMenuItem("Könyvtár betöltése Excelből (97)");
    public final JMenuItem MENÜPONT_EXCEL_MENTÉS = new JMenuItem("Könyvtár mentése Excelbe (97)");
    public final JMenuItem MENÜPONT_ISBN_KERES = new JMenuItem("ISBN keresés");
    private final JMenu MENÜ_FÁJL = new JMenu("Fájl");
    public final JButton TOOLBAR_UJ_KONYV = new JButton(new ImageIcon(Resources.getResource("icons/ujkonyv.gif")));
    public final JButton TOOLBAR_KONYV_TOROL = new JButton(new ImageIcon(Resources.getResource("icons/töröl.gif")));
    final JLabel TOOLBAR_KERES_LABEL = new JLabel("Keresés: ");
    public final JTextField TOOLBAR_KERES = new JTextField(5);

    @Getter
    private final JToolBar toolBar = new JToolBar("Alapfunkciók");

    @Getter
    private final JMenuBar menuBar = new JMenuBar();

    public IdaKönyvtárMenüAndToolBar()
    {
        super();
        menuBar.add(MENÜ_FÁJL);
        MENÜ_FÁJL.add(MENÜPONT_EXCEL_TÖLTÉS);
        MENÜ_FÁJL.add(MENÜPONT_EXCEL_MENTÉS);
        menuBar.add(MENÜPONT_ISBN_KERES);

        toolBar.add(TOOLBAR_UJ_KONYV);
        TOOLBAR_UJ_KONYV.setToolTipText("Új könyv hozzáadása");
        toolBar.add(TOOLBAR_KONYV_TOROL);
        TOOLBAR_KONYV_TOROL.setToolTipText("Könyv törlése");
        TOOLBAR_KONYV_TOROL.setEnabled(false);
        toolBar.add(TOOLBAR_KERES_LABEL);
        toolBar.add(TOOLBAR_KERES);
    }

}
