package eu.eyan.idakonyvtar.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

public class Könyv
{
    final List<String> values = newArrayList();

    public Könyv(int oszlopSzám)
    {
        // TODO az arraylist.set metódus miatt :(
        for (int i = 0; i < oszlopSzám; i++)
        {
            values.add("");
        }
    }

    public void setValue(int index, final String value)
    {
        // FIXME: AutoCompleteDecorator Problem: Disgusting Hack but works...
        if (value != null)
        {
            this.values.set(index, value);
        }
    }

    public String getValue(int index)
    {
        return values.get(index);
    }

}
