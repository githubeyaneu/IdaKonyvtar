package eu.eyan.idakonyvtar.model

import com.google.common.collect.Sets.newHashSet

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import scala.collection.JavaConverters._

import com.jgoodies.binding.value.ValueModel

import scala.collection.mutable.Set
import eu.eyan.log.Log

class BookFieldValueModel(columnIndex: Int, model: Book) extends ValueModel with PropertyChangeListener {

  model.addPropertyChangeListener(this);

  val listeners: Set[PropertyChangeListener] = Set()

  def getValue() = model.getValue(columnIndex)

  def setValue(newValue: Object) = {
    Log.debug(newValue.toString)
    model.setValue(columnIndex, newValue.asInstanceOf[String])
  }

  def addValueChangeListener(listener: PropertyChangeListener) = listeners += listener

  def removeValueChangeListener(listener: PropertyChangeListener) = listeners -= listener

  def propertyChange(evt: PropertyChangeEvent) =
    evt match {
      case bookEvent: Book.BookPropertyChangeEvent =>
        if (bookEvent.getColumnIndex() == this.columnIndex) listeners.foreach(listener => listener.propertyChange(evt))
    }
}