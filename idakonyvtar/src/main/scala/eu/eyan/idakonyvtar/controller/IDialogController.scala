package eu.eyan.idakonyvtar.controller;

import java.awt.Window;

trait IDialogController[INPUT, OUTPUT] extends IController[INPUT, OUTPUT] {
  def onOk():Unit

  def onCancel():Unit

  def addResizeListener(window: Window): Unit
}