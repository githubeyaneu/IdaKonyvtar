package eu.eyan.idakonyvtar.view;

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

public class KönyvtárMenüAndToolBar
{
    public static final String KÖNYV_TÖRLÉSE = "Könyv törlése";
    public static final String ÚJ_KÖNYV_HOZZÁADÁSA = "Új könyv hozzáadása";
    public static final String SZŰRÉS = "Szűrés: ";
    public static final String KÖNYVTÁR_MENTÉSE = "Könyvtár mentése";
    public static final String KÖNYVTÁR_BETÖLTÉSE = "Könyvtár betöltése";
    public static final String ISBN_KERESÉS = "ISBN keresés";
    public static final String FILE = "Fájl";

    public final JMenuItem MENÜPONT_EXCEL_TÖLTÉS = new JMenuItem(KÖNYVTÁR_BETÖLTÉSE);
    public final JMenuItem MENÜPONT_EXCEL_MENTÉS = new JMenuItem(KÖNYVTÁR_MENTÉSE);
    private final JMenu MENÜ_FÁJL = new JMenu(FILE);
    public final JButton TOOLBAR_UJ_KONYV = new JButton("Új könyv", new ImageIcon(Resources.getResource("icons/ujkonyv.gif")));
    public final JButton TOOLBAR_KONYV_TOROL = new JButton("Törlés", new ImageIcon(Resources.getResource("icons/töröl.gif")));
    final JLabel TOOLBAR_KERES_LABEL = new JLabel(SZŰRÉS);
    public final JTextField TOOLBAR_KERES = new JTextField(5);

    @Getter
    private final JToolBar toolBar = new JToolBar("Alapfunkciók");

    @Getter
    private final JMenuBar menuBar = new JMenuBar();

    public KönyvtárMenüAndToolBar()
    {
        super();
        menuBar.add(MENÜ_FÁJL);
        MENÜ_FÁJL.setName(FILE);

        MENÜ_FÁJL.add(MENÜPONT_EXCEL_TÖLTÉS);
        MENÜPONT_EXCEL_TÖLTÉS.setName(KÖNYVTÁR_BETÖLTÉSE);

        MENÜ_FÁJL.add(MENÜPONT_EXCEL_MENTÉS);
        MENÜPONT_EXCEL_MENTÉS.setName(KÖNYVTÁR_MENTÉSE);

        toolBar.add(TOOLBAR_UJ_KONYV);
        TOOLBAR_UJ_KONYV.setName(ÚJ_KÖNYV_HOZZÁADÁSA);
        TOOLBAR_UJ_KONYV.setToolTipText(ÚJ_KÖNYV_HOZZÁADÁSA);

        toolBar.add(TOOLBAR_KONYV_TOROL);
        TOOLBAR_KONYV_TOROL.setToolTipText(KÖNYV_TÖRLÉSE);
        TOOLBAR_KONYV_TOROL.setName(KÖNYV_TÖRLÉSE);
        TOOLBAR_KONYV_TOROL.setEnabled(false);

        toolBar.add(TOOLBAR_KERES_LABEL);
        toolBar.add(TOOLBAR_KERES);
        TOOLBAR_KERES.setName(SZŰRÉS);
        TOOLBAR_KERES.setSize(200, TOOLBAR_KERES.getHeight());

    }
}
