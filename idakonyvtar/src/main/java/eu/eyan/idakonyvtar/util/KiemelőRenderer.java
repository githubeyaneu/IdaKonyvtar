package eu.eyan.idakonyvtar.util;

import java.awt.Component;
import java.util.regex.Matcher;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class KiemelőRenderer implements TableCellRenderer
{
    private JLabel jLabel = new JLabel();

    private String kiemelendőSzövegÉkezetekNélkül = "";

    private Matcher matcher;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        String szöveg = (String) value;
        String szövegÉkezetekNélkül = MagyarÉkezetHelper.ékezetNélkül(szöveg).toLowerCase();
        if (!kiemelendőSzövegÉkezetekNélkül.equals("") && szövegÉkezetekNélkül.contains(kiemelendőSzövegÉkezetekNélkül))
        {
            matcher.reset(szövegÉkezetekNélkül);
            StringBuilder html = new StringBuilder();
            html.append("<html>");
            int start = 0;
            while (matcher.find())
            {
                html.append(szöveg.substring(start, matcher.start()));
                html.append("<span style=\"background-color:#F2F5A9;\">");
                html.append(szöveg.substring(matcher.start(), matcher.end()));
                html.append("</span>");
                start = matcher.end();
            }
            html.append(szöveg.substring(start, szöveg.length()));
            html.append("</html>");
            jLabel.setText(html.toString());
        }
        else
        {
            jLabel.setText(szöveg);
        }
        return jLabel;
    }

    public void setKiemelendőSzöveg(String kiemelendőSzöveg)
    {
        this.kiemelendőSzövegÉkezetekNélkül = MagyarÉkezetHelper.ékezetNélkül(kiemelendőSzöveg).toLowerCase();
        this.matcher = MagyarÉkezetHelper.szűrőPattern(this.kiemelendőSzövegÉkezetekNélkül).matcher("");
    }
}
