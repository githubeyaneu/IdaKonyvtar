package eu.eyan.idakonyvtar.util;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;

object SpecialCharacterHelper {

  def filterPattern(filterText: String) = Pattern.compile(withoutSpecChars(filterText), Pattern.CASE_INSENSITIVE)

  def withoutSpecChars(text: String) = if (text == null) null else
    Normalizer.normalize(text, Form.NFD)
      .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
}