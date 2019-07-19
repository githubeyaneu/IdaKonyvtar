package eu.eyan.idakonyvtar.model

import java.awt.image.BufferedImage

import com.jgoodies.binding.beans.Model
import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.idakonyvtar.text.TechnicalTextsIda.EMPTY_STRING
import eu.eyan.util.excel.ExcelColumn

trait BookFieldType
case object Multifield extends BookFieldType
case object Autocomplete extends BookFieldType
case object InTable extends BookFieldType
case object Remember extends BookFieldType
case object Picture extends BookFieldType

case class BookField (excelColumn: ExcelColumn, fieldName: String, private val fieldTypes : List[BookFieldType], marcCodes: Array[Marc]) {
  lazy val isMulti = fieldTypes.contains(Multifield)
  lazy val isAutocomplete = fieldTypes.contains(Autocomplete)
  lazy val isShowInTable = fieldTypes.contains(InTable)
  lazy val isPicture = fieldTypes.contains(Picture)
  lazy val isRemember = fieldTypes.contains(Remember)
}

//class BookPropertyChangeEvent(source: Object, propertyName: String, oldValue: Object, newValue: Object, val field: BookField) extends PropertyChangeEvent(source, propertyName, oldValue, newValue)

object Book {
  def apply(fields: List[(BookField, String)]): Book = new Book(Map(fields: _*)) //TODO delete mutable

  def empty(fields: List[BookField]): Book = Book(fields.map((_, EMPTY_STRING)))
}

class Book private (
  private val fields: Map[BookField, String]        = Map(),
  private val images: Map[BookField, BufferedImage] = Map()) extends Model {

  def getValue(field: BookField) = fields.getOrElse(field, "") // TODO is getOrElse ok??? // it was done only for tests

  def withPictures(pictures: Map[BookField, BufferedImage]) = new Book(fields, pictures) 
  def getImage(field: BookField) = images.get(field)
  def getValues: Map[BookField, String] = fields

  override def toString: String = fields.mkString(" - ")
}