package eu.eyan.idakonyvtar.controller;

import java.awt.Window;

trait IDialogController[INPUT, OUTPUT] extends IController[INPUT, OUTPUT] {
  def onOk()

  def onCancel()

  def addResizeListener(window: Window)
}