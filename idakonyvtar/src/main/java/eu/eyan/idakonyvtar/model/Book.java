package eu.eyan.idakonyvtar.model;

import static com.google.common.collect.Lists.newArrayList;

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

import com.jgoodies.binding.beans.Model;

public class Book extends Model implements Cloneable
{
    private static final long serialVersionUID = 1L;
    final List<String> values = newArrayList();

    public Book(int columnSzám)
    {
        // TODO az arraylist.set metódus miatt :(
        for (int i = 0; i < columnSzám; i++)
        {
            values.add("");
        }
    }

    public Book(Book book)
    {
        this(book.values.size());
        Collections.copy(this.values, book.values);
    }

    public void setValue(int columnIndex, final String value)
    {
        String oldValue = this.values.get(columnIndex);
        // FIXME: AutoCompleteDecorator Problem: Disgusting Hack but works...
        if (value != null /* && !value.equals("") */)
        {
            this.values.set(columnIndex, value);
            firePropertyChange(new BookPropertyChangeEvent(this, "PROP", oldValue, value, columnIndex));
        }
    }

    public String getValue(int index)
    {
        return values.get(index);
    }

    public static class BookPropertyChangeEvent extends PropertyChangeEvent
    {
        private static final long serialVersionUID = 1L;
        @Getter
        private int columnIndex;

        public BookPropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue, int columnIndex)
        {
            super(source, propertyName, oldValue, newValue);
            this.columnIndex = columnIndex;
        }
    }

    public static class Builder
    {
        private Book book;

        public Builder(int columnCount)
        {
            this.book = new Book(columnCount);
        }

        public Builder withValue(int columnIndex, String value)
        {
            this.book.setValue(columnIndex, value);
            return this;
        }

        public Book build()
        {
            return this.book;
        }
    }
}