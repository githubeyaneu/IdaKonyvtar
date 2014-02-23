package eu.eyan.idakonyvtar.model;

import static com.google.common.collect.Lists.newArrayList;

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

import com.jgoodies.binding.beans.Model;

public class Könyv extends Model implements Cloneable
{
    private static final long serialVersionUID = 1L;
    final List<String> values = newArrayList();

    public Könyv(int oszlopSzám)
    {
        // TODO az arraylist.set metódus miatt :(
        for (int i = 0; i < oszlopSzám; i++)
        {
            values.add("");
        }
    }

    public Könyv(Könyv könyv)
    {
        this(könyv.values.size());
        Collections.copy(this.values, könyv.values);
    }

    public void setValue(int index, final String value)
    {
        String oldValue = this.values.get(index);
        // FIXME: AutoCompleteDecorator Problem: Disgusting Hack but works...
        if (value != null && !value.equals(""))
        {
            this.values.set(index, value);
            firePropertyChange(new KönyvPropertyChangeEvent(this, "PROP", oldValue, value, index));
        }
    }

    public String getValue(int index)
    {
        return values.get(index);
    }

    public static class KönyvPropertyChangeEvent extends PropertyChangeEvent
    {
        private static final long serialVersionUID = 1L;
        @Getter
        private int oszlopIndex;

        public KönyvPropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue, int oszlopIndex)
        {
            super(source, propertyName, oldValue, newValue);
            this.oszlopIndex = oszlopIndex;
        }

    }
}
