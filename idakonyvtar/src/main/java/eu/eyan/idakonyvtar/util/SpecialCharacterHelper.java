package eu.eyan.idakonyvtar.util;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;

public class SpecialCharacterHelper
{

    public static Pattern filterPattern(String filterText)
    {
        return Pattern.compile(SpecialCharacterHelper.withoutSpecChars(filterText), Pattern.CASE_INSENSITIVE);
    }

    public static String withoutSpecChars(String text)
    {
        return text == null ? null
                : Normalizer.normalize(text, Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

}
