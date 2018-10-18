package eu.eyan.idakonyvtar.util;

import java.text.Collator
import java.util.Locale

import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._

object BookHelper {

  val COLLATOR = Collator.getInstance(new Locale("hu"))
  COLLATOR.setStrength(Collator.SECONDARY); // a == A, a < Ä

}