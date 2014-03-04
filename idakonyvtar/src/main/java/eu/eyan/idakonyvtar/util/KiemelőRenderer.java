package eu.eyan.idakonyvtar.util;

import java.awt.Component;
import java.util.regex.Matcher;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class KiemelőRenderer extends DefaultTableCellRenderer
{
    public static final String KIEMELÉS_END_TAG = "</span>";
    public static final String KIEMELÉS_START_TAG = "<span style=\"background-color:#F2F5A9;\">";
    public static final String HTML_END_TAG = "</html>";
    public static final String HTML_START_TAG = "<html>";

    private static final long serialVersionUID = 1L;

    private String kiemelendőSzövegÉkezetekNélkül = "";

    private Matcher matcher;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String szöveg = (String) value;
        String szövegÉkezetekNélkül = MagyarÉkezetHelper.ékezetNélkül(szöveg).toLowerCase();
        if (!kiemelendőSzövegÉkezetekNélkül.equals("") && szövegÉkezetekNélkül.contains(kiemelendőSzövegÉkezetekNélkül))
        {
            matcher.reset(szövegÉkezetekNélkül);
            StringBuilder html = new StringBuilder();
            html.append(HTML_START_TAG);
            int start = 0;
            while (matcher.find())
            {
                html.append(szöveg.substring(start, matcher.start()));
                html.append(KIEMELÉS_START_TAG);
                html.append(szöveg.substring(matcher.start(), matcher.end()));
                html.append(KIEMELÉS_END_TAG);
                start = matcher.end();
            }
            html.append(szöveg.substring(start, szöveg.length()));
            html.append(HTML_END_TAG);
            setText(html.toString());
        }
        else
        {
            setText(szöveg);
        }
        return this;
    }

    public void setKiemelendőSzöveg(String kiemelendőSzöveg)
    {
        this.kiemelendőSzövegÉkezetekNélkül = MagyarÉkezetHelper.ékezetNélkül(kiemelendőSzöveg).toLowerCase();
        this.matcher = MagyarÉkezetHelper.szűrőPattern(this.kiemelendőSzövegÉkezetekNélkül).matcher("");
    }
}
