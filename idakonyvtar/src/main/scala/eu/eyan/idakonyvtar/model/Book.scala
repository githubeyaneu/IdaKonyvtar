package eu.eyan.idakonyvtar.model;

import com.google.common.collect.Lists.newArrayList

import java.beans.PropertyChangeEvent;
import java.util.Collections;

import com.jgoodies.binding.beans.Model;

object Book {
  def apply(columnCount: Int): Book = {
    val vals: java.util.List[String] = newArrayList()
    for (i <- 0 until columnCount) vals.add("")
    new Book(newArrayList(vals))
  }

  def apply(book: Book): Book = {
    val newBook = Book(book.values.size())
    Collections.copy(newBook.values, book.values)
    newBook
  }

  class BookPropertyChangeEvent(source: Object, propertyName: String,
                                oldValue: Object, newValue: Object, val columnIndex: Int) extends PropertyChangeEvent(source, propertyName, oldValue, newValue) {
    def getColumnIndex() = columnIndex
  }

  class Builder(columnCount: Int) {
    val book = Book(columnCount)

    def withValue(columnIndex: Int, value: String): Builder = {
      book.setValue(columnIndex, value)
      this
    }

    def build(): Book = book
  }
}

class Book(val values: java.util.List[String]) extends Model {

  def setValue(columnIndex: Int, value: String) = {
    val oldValue = this.values.get(columnIndex);
    // FIXME: AutoCompleteDecorator Problem: Disgusting Hack but works...
    if (value != null /* && !value.equals("") */ ) {
      this.values.set(columnIndex, value)
      firePropertyChange(new Book.BookPropertyChangeEvent(this, "PROP", oldValue, value, columnIndex))
    }
  }

  def getValue(columnIndex: Int) = values.get(columnIndex)

}