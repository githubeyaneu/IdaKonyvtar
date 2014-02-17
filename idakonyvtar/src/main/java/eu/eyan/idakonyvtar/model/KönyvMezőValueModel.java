package eu.eyan.idakonyvtar.model;

import static com.google.common.collect.Sets.newHashSet;

import java.beans.PropertyChangeListener;
import java.util.Set;

import com.jgoodies.binding.value.ValueModel;

public class KönyvMezőValueModel implements ValueModel
{
    private int oszlopIndex;
    private Könyv model;
    private Set<PropertyChangeListener> listeners;

    public KönyvMezőValueModel(int oszlopIndex, Könyv könyv)
    {
        this.oszlopIndex = oszlopIndex;
        this.model = könyv;
    }

    @Override
    public Object getValue()
    {
        return model.getValue(oszlopIndex);
    }

    @Override
    public void setValue(Object newValue)
    {
        model.setValue(oszlopIndex, (String) newValue);
    }

    @Override
    public void addValueChangeListener(PropertyChangeListener listener)
    {
        if (listeners == null)
        {
            listeners = newHashSet();
        }
        listeners.add(listener);
    }

    @Override
    public void removeValueChangeListener(PropertyChangeListener listener)
    {
        listeners.remove(listener);
    }
}
