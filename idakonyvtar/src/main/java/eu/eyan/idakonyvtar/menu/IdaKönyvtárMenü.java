package eu.eyan.idakonyvtar.menu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class IdaKönyvtárMenü extends JMenuBar
{
    private static final long serialVersionUID = 1L;
    public final JMenuItem EXCEL_TÖLTÉS = new JMenuItem("Könyvtár betöltése Excelből (97)");
    public final JMenuItem EXCEL_MENTÉS = new JMenuItem("Könyvtár mentése Excelbe (97)");
    public final JMenuItem ISBN_KERES = new JMenuItem("ISBN keresés");
    private final JMenu FÁJL_MENÜ = new JMenu("Fájl");

    public IdaKönyvtárMenü()
    {
        super();
        add(FÁJL_MENÜ);
        FÁJL_MENÜ.add(EXCEL_TÖLTÉS);
        FÁJL_MENÜ.add(EXCEL_MENTÉS);
        add(ISBN_KERES);
    }

}
