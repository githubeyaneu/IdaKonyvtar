package eu.eyan.idakonyvtar.model;

import static com.google.common.collect.Sets.newHashSet;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import com.jgoodies.binding.value.ValueModel;

public class BookFieldValueModel implements ValueModel, PropertyChangeListener
{
    private int columnIndex;
    private Book model;
    private Set<PropertyChangeListener> listeners = newHashSet();

    public BookFieldValueModel(int columnIndex, Book book)
    {
        this.columnIndex = columnIndex;
        this.model = book;
        model.addPropertyChangeListener(this);
    }

    @Override
    public Object getValue()
    {
        return model.getValue(columnIndex);
    }

    @Override
    public void setValue(Object newValue)
    {
        model.setValue(columnIndex, (String) newValue);
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

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt instanceof Book.BookPropertyChangeEvent)
        {
            Book.BookPropertyChangeEvent bookEvent = (Book.BookPropertyChangeEvent) evt;
            if (bookEvent.getColumnIndex() == this.columnIndex)
            {
                for (PropertyChangeListener listener : listeners)
                {
                    listener.propertyChange(evt);
                }
            }
        }

    }
}
