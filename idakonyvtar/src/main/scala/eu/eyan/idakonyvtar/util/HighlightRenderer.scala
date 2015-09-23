package eu.eyan.idakonyvtar.util;

import java.awt.Component;
import java.util.regex.Matcher;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

object HighlightRenderer extends DefaultTableCellRenderer {
  val HIGHLIGHT_END_TAG = "</span>"
  val HIGHLIGHT_START_TAG = "<span style=\"background-color:#F2F5A9;\">"
  val HTML_END_TAG = "</html>"
  val HTML_START_TAG = "<html>"
}

class HighlightRenderer extends DefaultTableCellRenderer {

  var textToHighlighWithoutSpecChars = ""

  var matcher: Matcher = null

  override def getTableCellRendererComponent(table: JTable, value: Object, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    val text = value.asInstanceOf[String]
    val textWithoutSpecChars = SpecialCharacterHelper.withoutSpecChars(text).toLowerCase()
    if (!textToHighlighWithoutSpecChars.equals("") && textWithoutSpecChars.contains(textToHighlighWithoutSpecChars)) {
      matcher.reset(textWithoutSpecChars)
      val html = new StringBuilder()
      html.append(HighlightRenderer.HTML_START_TAG)
      var start = 0
      while (matcher.find()) {
        html.append(text.substring(start, matcher.start()))
        html.append(HighlightRenderer.HIGHLIGHT_START_TAG)
        html.append(text.substring(matcher.start(), matcher.end()))
        html.append(HighlightRenderer.HIGHLIGHT_END_TAG)
        start = matcher.end()
      }
      html.append(text.substring(start, text.length()))
      html.append(HighlightRenderer.HTML_END_TAG)
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
