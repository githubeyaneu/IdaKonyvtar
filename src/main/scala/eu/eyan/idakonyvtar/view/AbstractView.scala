package eu.eyan.idakonyvtar.view

import java.awt.Component

import com.jgoodies.forms.layout.{FormLayout, RowSpec}

object AbstractView {
  def addRow(layout: FormLayout, spec: String): Int = {
    spec.split(",").foreach(row => layout.appendRow(RowSpec.decode(row)))
    spec.split(",").length
  }
}

abstract class AbstractView extends IView {
  var view: Component = _

  override def getComponent: Component = {
    if (this.view == null) this.view = createViewComponent
    view
  }

  protected def createViewComponent: Component
}