package eu.eyan.idakonyvtar.util;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;

public class MagyarÉkezetHelper
{

    public static Pattern szűrőPattern(String szűrőSzöveg)
    {
        return Pattern.compile(MagyarÉkezetHelper.ékezetNélkül(szűrőSzöveg), Pattern.CASE_INSENSITIVE);
    }

    public static String ékezetNélkül(String text)
    {
        return text == null ? null
                : Normalizer.normalize(text, Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

}
