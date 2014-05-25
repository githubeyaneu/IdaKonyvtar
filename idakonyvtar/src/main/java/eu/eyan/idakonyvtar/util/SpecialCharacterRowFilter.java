package eu.eyan.idakonyvtar.util;

import java.util.regex.Matcher;

import org.jdesktop.swingx.sort.RowFilters.GeneralFilter;

public class SpecialCharacterRowFilter extends GeneralFilter
{
    private Matcher matcher;

    public SpecialCharacterRowFilter(String filterText)
    {
        matcher = SpecialCharacterHelper.filterPattern(filterText).matcher("");
    }

    @Override
    protected boolean include(javax.swing.RowFilter.Entry<? extends Object, ? extends Object> value, int index)
    {
        matcher.reset(SpecialCharacterHelper.withoutSpecChars(value.getStringValue(index)));
        return matcher.find();
    }
}
