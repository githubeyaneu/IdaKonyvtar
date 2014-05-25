package eu.eyan.idakonyvtar.util;

import java.awt.Component;
import java.util.regex.Matcher;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class HighlightRenderer extends DefaultTableCellRenderer
{
    public static final String HIGHLIGHT_END_TAG = "</span>";
    public static final String HIGHLIGHT_START_TAG = "<span style=\"background-color:#F2F5A9;\">";
    public static final String HTML_END_TAG = "</html>";
    public static final String HTML_START_TAG = "<html>";

    private static final long serialVersionUID = 1L;

    private String textToHighlighWithoutSpecChars = "";

    private Matcher matcher;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String text = (String) value;
        String textWithoutSpecChars = SpecialCharacterHelper.withoutSpecChars(text).toLowerCase();
        if (!textToHighlighWithoutSpecChars.equals("") && textWithoutSpecChars.contains(textToHighlighWithoutSpecChars))
        {
            matcher.reset(textWithoutSpecChars);
            StringBuilder html = new StringBuilder();
            html.append(HTML_START_TAG);
            int start = 0;
            while (matcher.find())
            {
                html.append(text.substring(start, matcher.start()));
                html.append(HIGHLIGHT_START_TAG);
                html.append(text.substring(matcher.start(), matcher.end()));
                html.append(HIGHLIGHT_END_TAG);
                start = matcher.end();
            }
            html.append(text.substring(start, text.length()));
            html.append(HTML_END_TAG);
            setText(html.toString());
        }
        else
        {
            setText(text);
        }
        return this;
    }

    public void setHighlightText(String textToHighlight)
    {
        this.textToHighlighWithoutSpecChars = SpecialCharacterHelper.withoutSpecChars(textToHighlight).toLowerCase();
        this.matcher = SpecialCharacterHelper.filterPattern(this.textToHighlighWithoutSpecChars).matcher("");
    }
}
