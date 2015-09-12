package eu.eyan.idakonyvtar.util;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import eu.eyan.idakonyvtar.model.Book;

public class BookHelper
{
    public static final String LISTA_SEPARATOR = " + ";
    public static final String LISTA_SEPARATOR_REGEX = LISTA_SEPARATOR.replace("+", "\\+");

    final static Collator collator = Collator.getInstance(new Locale("hu"));

    public static List<String> getColumnList(List<Book> bookList, int columnIndex)
    {
        Set<String> set = newHashSet("");
        for (Book book : bookList)
        {
            if (book.getValue(columnIndex) != null)
            {
                List<String> values = newArrayList(book.getValue(columnIndex).split(LISTA_SEPARATOR_REGEX));
                values.forEach((String s) -> s.trim());
                set.addAll(values);
            }
        }
        List<String> list = newArrayList(set);

        collator.setStrength(Collator.SECONDARY);// a == A, a < Ä
        Collections.sort(list, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return collator.compare(o1, o2);
            }
        });
        return list;
    }
}