package eu.eyan.idakonyvtar.util;

import java.text.Collator
import java.util.Locale

import eu.eyan.idakonyvtar.model.Book

object BookHelper {
  val LISTA_SEPARATOR = " + "
  val LISTA_SEPARATOR_REGEX = LISTA_SEPARATOR.replace("+", "\\+")

  val COLLATOR = Collator.getInstance(new Locale("hu"))
  COLLATOR.setStrength(Collator.SECONDARY); // a == A, a < Ä

  def getColumnList(bookList: Seq[Book], columnIndex: Int) = {
    bookList
      .filter(_.getValue(columnIndex) != null) // only not nulls
      .map(_.getValue(columnIndex)) // get the values of the column
      .map(_.split(LISTA_SEPARATOR_REGEX)) // get all values if multifield
      .flatten // take the whole list
      .map(_.trim)
      .distinct
      .sortWith((s1: String, s2: String) => COLLATOR.compare(s1, s2) < 0)
  }
}