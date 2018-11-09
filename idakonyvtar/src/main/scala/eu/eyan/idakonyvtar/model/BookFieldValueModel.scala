package eu.eyan.idakonyvtar.model

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import scala.collection.mutable.Set

import com.jgoodies.binding.value.ValueModel

import eu.eyan.log.Log

//TODO delete after
class BookFieldValueModel(field: BookField, book: Book) extends ValueModel with PropertyChangeListener {
  def addValueChangeListener(listener: PropertyChangeListener) = listeners += listener

  def removeValueChangeListener(listener: PropertyChangeListener) = listeners -= listener

  def propertyChange(evt: PropertyChangeEvent) = evt match { case bookEvent: BookPropertyChangeEvent => if (bookEvent.field == field) listeners.foreach(_.propertyChange(evt)) }

  def getValue() = book.getValue(field)

  def setValue(newValue: Object) = {
    Log.debug(newValue.toString)
    book.setValue(field)(newValue.asInstanceOf[String])
  }

  private val listeners: Set[PropertyChangeListener] = Set()

  book.addPropertyChangeListener(this)
}