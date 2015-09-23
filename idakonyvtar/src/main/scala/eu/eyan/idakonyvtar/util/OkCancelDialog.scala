package eu.eyan.idakonyvtar.util;

import java.awt.Window;

import javax.swing.JDialog;

class OkCancelDialog(owner: Window) extends JDialog(owner) {
  var ok = false;
  def isOk() = ok
  def setOk(ok: Boolean) = this.ok = ok
}