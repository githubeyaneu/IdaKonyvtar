package eu.eyan.idakonyvtar.util;

import com.google.common.collect.Lists.newArrayList;
import com.google.common.collect.Sets.newHashSet;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import scala.collection.JavaConversions._

import eu.eyan.idakonyvtar.model.Book;

object BookHelper {
  val LISTA_SEPARATOR = " + "
  val LISTA_SEPARATOR_REGEX = LISTA_SEPARATOR.replace("+", "\\+")

  val COLLATOR = Collator.getInstance(new Locale("hu"))

  def getColumnList(bookList: java.util.List[Book], columnIndex: Int): java.util.List[String] = {
    val set: java.util.Set[String] = newHashSet("")
    for (book <- bookList) {
      if (book.getValue(columnIndex) != null) {
        book.getValue(columnIndex).split(LISTA_SEPARATOR_REGEX).map(_.trim()).foreach(set.add(_))
      }
    }
    val list = newArrayList(set)

    COLLATOR.setStrength(Collator.SECONDARY); // a == A, a < Ä
    Collections.sort(list, new Comparator[String]() {
      override def compare(o1: String, o2: String): Int = {
        COLLATOR.compare(o1, o2)
      }
    })
    return list;
  }
}
