package eu.eyan.idakonyvtar.view;

import java.awt.Component;

abstract class AbstractView extends IView {
  var view: Component = null

  override def getComponent(): Component = {
    if (this.view == null)
      this.view = createViewComponent();
    return this.view;
  }

  protected def createViewComponent(): Component
}