package eu.eyan.idakonyvtar.util;

import java.util.regex.Matcher;

import org.jdesktop.swingx.sort.RowFilters.GeneralFilter;

public class MagyarRowFilter extends GeneralFilter
{
    private Matcher matcher;

    public MagyarRowFilter(String szűrőSzöveg)
    {
        matcher = MagyarÉkezetHelper.szűrőPattern(szűrőSzöveg).matcher("");
    }

    @Override
    protected boolean include(javax.swing.RowFilter.Entry<? extends Object, ? extends Object> value, int index)
    {
        matcher.reset(MagyarÉkezetHelper.ékezetNélkül(value.getStringValue(index)));
        return matcher.find();
    }
}
