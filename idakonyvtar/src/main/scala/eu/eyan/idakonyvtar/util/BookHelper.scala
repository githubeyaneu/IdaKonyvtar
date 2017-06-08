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
    val columnList = bookList
      .map(_.getValue(columnIndex)) // get the values of the column
      .filter(_ != null) // only not nulls
      .map(s => if (s.contains(LISTA_SEPARATOR)) s.split(LISTA_SEPARATOR_REGEX) else Array(s)) // get all values if multifield
      .flatten // take the whole list
      .++:(List("")) // empty is always the default option
      .map(_.trim)
<<<<<<< HEAD
      .distinct
      .sortWith((s1: String, s2: String) => COLLATOR.compare(s1, s2) < 0)
      .toList
=======
      // .distinct //do distinct in ac
      //      .sortWith((s1: String, s2: String) => COLLATOR.compare(s1, s2) < 0) //autocomplete does sorting
      .toList
    columnList
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
  }
}