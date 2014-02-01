package eu.eyan.idakonyvtar;

import static javax.swing.JFileChooser.APPROVE_OPTION;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class IdaKönyvtárMenü extends JMenuBar implements ActionListener
{
    private static final long serialVersionUID = 1L;
    private static final JMenuItem IMPORT_EXCEL = new JMenuItem("Excel Import (97)");
    private static final JMenu FÁJL_MENÜ = new JMenu("Fájl");

    private final IdaKönyvtár idaKönyvtár;

    public IdaKönyvtárMenü(final IdaKönyvtár idaKönyvtár)
    {
        this.idaKönyvtár = idaKönyvtár;
        add(FÁJL_MENÜ);
        FÁJL_MENÜ.add(IMPORT_EXCEL);
        IMPORT_EXCEL.addActionListener(this);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        if (e.getSource() == IMPORT_EXCEL)
        {
            JFileChooser jFileChooser = new JFileChooser();
            if (jFileChooser.showOpenDialog(this) == APPROVE_OPTION)
            {
                idaKönyvtár.importExcel(jFileChooser.getSelectedFile());
            }
        }
    }
}
