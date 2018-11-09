package eu.eyan.idakonyvtar.model;

import java.beans.PropertyChangeEvent

import scala.collection.mutable.MutableList

import com.jgoodies.binding.beans.Model
import eu.eyan.log.Log
import eu.eyan.log.Log
import java.awt.Image
import scala.collection.mutable.Map
import java.awt.image.BufferedImage
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.util.excel.ExcelColumn

trait BookFieldType
case object Multifield extends BookFieldType
case object Autocomplete extends BookFieldType
case object InTable extends BookFieldType
case object Remember extends BookFieldType
case object Picture extends BookFieldType

case class BookField (excelColumn: ExcelColumn, fieldName: String, private val fieldTypes : List[BookFieldType], val marcCodes: Array[Marc]) {
  lazy val isMulti = fieldTypes.contains(Multifield)
  lazy val isAutocomplete = fieldTypes.contains(Autocomplete)
  lazy val isShowInTable = fieldTypes.contains(InTable)
  lazy val isPicture = fieldTypes.contains(Picture)
  lazy val isRemember = fieldTypes.contains(Remember)
}

class BookPropertyChangeEvent(source: Object, propertyName: String, oldValue: Object, newValue: Object, val field: BookField) extends PropertyChangeEvent(source, propertyName, oldValue, newValue)

object Book {
  def apply(fields: List[(BookField, String)]): Book = new Book(Map(fields: _*))

  def empty(fields: List[BookField]): Book = Book(fields.map((_, EMPTY_STRING)))

  def copy(book: Book): Book = book.copy
}

// TODO: make it immutable. remove the binding framework
class Book private (
  private val fields: Map[BookField, String]        = Map(),
  private val images: Map[BookField, BufferedImage] = Map()) extends Model {

  def setValue(field: BookField)(value: String) = {
    val oldValue: String = getValue(field)
    fields.put(field, value)
    Log.trace("fire " + value)
    firePropertyChange(new BookPropertyChangeEvent(this, "PROP", oldValue, value, field))
  }

  def getValue(field: BookField) = fields.get(field).getOrElse("") // TODO is getOrElse ok??? // it was done only for tests

  def setImage(field: BookField)(image: BufferedImage) = images.put(field, image)
  def getImage(field: BookField) = images.get(field)

  override def toString(): String = fields.mkString(" - ")

  protected def copy = new Book(fields.clone, images.clone)
}