package eu.eyan.idakonyvtar.model;

import java.beans.PropertyChangeEvent

import scala.collection.mutable.MutableList

import com.jgoodies.binding.beans.Model
import eu.eyan.log.Log
import eu.eyan.log.Log
import java.awt.Image
import scala.collection.mutable.Map
import java.awt.image.BufferedImage

object Book {
  def apply(columnCount: Int): Book =
    new Book(MutableList.fill[String](columnCount)(""), Map())

  def apply(book: Book): Book = new Book(book.values.clone(), book.images)

  class BookPropertyChangeEvent(source: Object, propertyName: String, oldValue: Object, newValue: Object, val columnIndex: Int)
    extends PropertyChangeEvent(source, propertyName, oldValue, newValue) {
    def getColumnIndex() = columnIndex
  }

  class Builder(columnCount: Int) {
    val book = Book(columnCount)

    def withValue(columnIndex: Int, value: String): Builder = {
      Log.debug(columnIndex + " " + value)
      book.setValue(columnIndex)(value)
      this
    }

    def build(): Book = book
  }
}

//TODO: make it (?) immutable
class Book(val values: MutableList[String], val images: Map[Int, BufferedImage]   ) extends Model {
  def setValue(columnIndex: Int)(value: String) = {
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