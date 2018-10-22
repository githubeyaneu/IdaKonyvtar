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
  def apply(fieldCount: Int): Book = new Book(MutableList.fill[String](fieldCount)(""), Map())
  def apply(book: Book): Book = new Book(book.values.clone, book.images.clone)
}

class BookPropertyChangeEvent(source: Object, propertyName: String, oldValue: Object, newValue: Object, val columnIndex: Int) extends PropertyChangeEvent(source, propertyName, oldValue, newValue)

//TODO: make it immutable! use BookField instead of String or better BookField->String map
class Book(private val values: MutableList[String], private val images: Map[Int, BufferedImage]) extends Model {
  def setValue(columnIndex: Int)(value: String) = {
    val oldValue: String = this.values(columnIndex);
    if (value != null) {
      values(columnIndex) = value
      Log.trace("fire " + value)
      firePropertyChange(new BookPropertyChangeEvent(this, "PROP", oldValue, value, columnIndex))
    }
  }

  def getValue(columnIndex: Int) = values(columnIndex)

  def setImage(columnIndex: Int)(image: BufferedImage) = images.put(columnIndex, image)
  def getImage(columnIndex: Int) = images.get(columnIndex)

  override def toString(): String = values.mkString(" - ")
}