package eu.eyan.idakonyvtar.model;

import com.google.common.collect.Sets.newHashSet;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import scala.collection.JavaConverters._

import com.jgoodies.binding.value.ValueModel;

class BookFieldValueModel(columnIndex: Int, model: Book) extends ValueModel with PropertyChangeListener {
  model.addPropertyChangeListener(this);

  val listeners: java.util.Set[PropertyChangeListener] = newHashSet()

  def getValue() = model.getValue(columnIndex)

  def setValue(newValue: Object) = model.setValue(columnIndex, newValue.asInstanceOf[String])

  def addValueChangeListener(listener: PropertyChangeListener) = listeners.add(listener)

  def removeValueChangeListener(listener: PropertyChangeListener) = listeners.remove(listener)

  def propertyChange(evt: PropertyChangeEvent) =
    if (evt.isInstanceOf[Book.BookPropertyChangeEvent]) {
      val bookEvent = evt.asInstanceOf[Book.BookPropertyChangeEvent]
      if (bookEvent.getColumnIndex() == this.columnIndex) listeners.asScala.foreach(listener => listener.propertyChange(evt))
    }
}