package eu.eyan.idakonyvtar.controller;

import javax.swing.JMenuBar;
import javax.swing.JToolBar;

trait IControllerWithMenu[INPUT, OUTPUT] extends IController[INPUT, OUTPUT] {
  def getMenuBar(): JMenuBar

  def getToolBar(): JToolBar
}