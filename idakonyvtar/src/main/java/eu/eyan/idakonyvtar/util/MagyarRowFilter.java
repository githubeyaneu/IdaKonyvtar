package eu.eyan.idakonyvtar.util;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdesktop.swingx.sort.RowFilters.GeneralFilter;

public class MagyarRowFilter extends GeneralFilter
{
    private Matcher matcher;

    public MagyarRowFilter(String szűrőSzöveg)
    {
        matcher = Pattern.compile(ékezetNélkül(szűrőSzöveg), Pattern.CASE_INSENSITIVE).matcher("");
    }

    @Override
    protected boolean include(javax.swing.RowFilter.Entry<? extends Object, ? extends Object> value, int index)
    {
        matcher.reset(ékezetNélkül(value.getStringValue(index)));
        return matcher.find();
    }

    public static String ékezetNélkül(String text)
    {
        return text == null ? null
                : Normalizer.normalize(text, Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
