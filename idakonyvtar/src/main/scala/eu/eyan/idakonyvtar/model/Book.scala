package eu.eyan.idakonyvtar.model;

import java.beans.PropertyChangeEvent

import scala.collection.mutable.MutableList

import com.jgoodies.binding.beans.Model
import eu.eyan.log.Log
import eu.eyan.log.Log

object Book {
  def apply(columnCount: Int): Book =
    new Book(MutableList.fill[String](columnCount)(""))

  def apply(book: Book): Book = new Book(book.values.clone())

  class BookPropertyChangeEvent(source: Object, propertyName: String, oldValue: Object, newValue: Object, val columnIndex: Int)
    extends PropertyChangeEvent(source, propertyName, oldValue, newValue) {
    def getColumnIndex() = columnIndex
  }

  class Builder(columnCount: Int) {
    val book = Book(columnCount)

    def withValue(columnIndex: Int, value: String): Builder = {
      Log.debug(columnIndex + " " + value)
      book.setValue(columnIndex, value)
      this
    }

    def build(): Book = book
  }
}

class Book(val values: MutableList[String]) extends Model {

  def setValue(columnIndex: Int, value: String) = {
    val oldValue: String = this.values(columnIndex);
    if (value != null) {
      values(columnIndex) = value
      Log.trace("fire " + value)
      firePropertyChange(new Book.BookPropertyChangeEvent(this, "PROP", oldValue, value, columnIndex))
    }
  }

  def getValue(columnIndex: Int) = values(columnIndex)

  override def toString(): String = values.mkString(" - ")
}