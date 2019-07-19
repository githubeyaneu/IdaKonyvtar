package eu.eyan.idakonyvtar.controller

import javax.swing.JToolBar

trait IControllerWithMenu[INPUT, OUTPUT] extends IController[INPUT, OUTPUT] {
  def getToolBar: JToolBar
}