package eu.eyan.idakonyvtar.view;

import java.awt.Component

trait FieldEditListener[EDITOR <: Component] {
  def fieldEdited(editorComponent: EDITOR):Unit
}