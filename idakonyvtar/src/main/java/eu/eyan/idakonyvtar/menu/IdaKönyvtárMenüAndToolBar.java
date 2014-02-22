package eu.eyan.idakonyvtar.menu;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import lombok.Getter;

import com.google.common.io.Resources;

public class IdaKönyvtárMenüAndToolBar
{
    public final JMenuItem EXCEL_TÖLTÉS = new JMenuItem("Könyvtár betöltése Excelből (97)");
    public final JMenuItem EXCEL_MENTÉS = new JMenuItem("Könyvtár mentése Excelbe (97)");
    public final JMenuItem ISBN_KERES = new JMenuItem("ISBN keresés");
    private final JMenu FÁJL_MENÜ = new JMenu("Fájl");
    public final JButton UJ_KONYV = new JButton(new ImageIcon(Resources.getResource("icons/ujkonyv.gif")));

    @Getter
    private final JToolBar toolBar = new JToolBar("Alapfunkciók");

    @Getter
    private final JMenuBar menuBar = new JMenuBar();

    public IdaKönyvtárMenüAndToolBar()
    {
        super();
        menuBar.add(FÁJL_MENÜ);
        FÁJL_MENÜ.add(EXCEL_TÖLTÉS);
        FÁJL_MENÜ.add(EXCEL_MENTÉS);
        menuBar.add(ISBN_KERES);

        toolBar.add(UJ_KONYV);
//        toolBar.add(new JButton(new ImageIcon(Resources.getResource("icons/load.gif"))));
//        toolBar.add(new JButton(new ImageIcon(Resources.getResource("icons/save.gif"))));

    }

}
