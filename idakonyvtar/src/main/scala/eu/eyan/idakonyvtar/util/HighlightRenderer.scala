package eu.eyan.idakonyvtar.util

import java.awt.Component
import java.util.regex.Matcher

import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

object HighlightRenderer extends DefaultTableCellRenderer {
  val HIGHLIGHT_START_TAG = "<span style=\"background-color:#F2F5A9;\">"
  val HIGHLIGHT_END_TAG = "</span>"
  val HTML_START_TAG = "<html>"
  val HTML_END_TAG = "</html>"
}

class HighlightRenderer extends DefaultTableCellRenderer {
  import eu.eyan.idakonyvtar.util.HighlightRenderer._

  var textToHighlighWithoutSpecChars = ""
  var matcher: Matcher = null

  override def getTableCellRendererComponent(table: JTable, value: Object, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    val text = value.asInstanceOf[String]
    val textWithoutSpecChars = SpecialCharacterHelper.withoutSpecChars(text).toLowerCase()
    if (!textToHighlighWithoutSpecChars.equals("") && textWithoutSpecChars.contains(textToHighlighWithoutSpecChars)) {
      matcher.reset(textWithoutSpecChars)
      val html = new StringBuilder()
      html.append(HTML_START_TAG)
      var start = 0
      while (matcher.find()) {
        html.append(text.substring(start, matcher.start()))
        html.append(HIGHLIGHT_START_TAG)
        html.append(text.substring(matcher.start(), matcher.end()))
        html.append(HIGHLIGHT_END_TAG)
        start = matcher.end()
      }
      html.append(text.substring(start, text.length()))
      html.append(HTML_END_TAG)
      setText(html.toString())
    } else {
      setText(text)
    }
    this
  }

  def setHighlightText(textToHighlight: String) = {
    this.textToHighlighWithoutSpecChars = SpecialCharacterHelper.withoutSpecChars(textToHighlight).toLowerCase();
    this.matcher = SpecialCharacterHelper.filterPattern(this.textToHighlighWithoutSpecChars).matcher("");
  }
}
