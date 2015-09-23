package eu.eyan.idakonyvtar.util;

import java.util.regex.Matcher;

import org.jdesktop.swingx.sort.RowFilters.GeneralFilter;

class SpecialCharacterRowFilter(filterText: String) extends GeneralFilter {
  val matcher = SpecialCharacterHelper.filterPattern(filterText).matcher("")

  def include(value: javax.swing.RowFilter.Entry[_, _], index: Int): Boolean = {
    matcher.reset(SpecialCharacterHelper.withoutSpecChars(value.getStringValue(index)))
    matcher.find()
  }
}