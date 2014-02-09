package eu.eyan.idakonyvtar.util;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import eu.eyan.idakonyvtar.model.Könyv;

public class KönyvHelper
{
    final static Collator collator = Collator.getInstance(new Locale("hu"));

    public static List<String> getMindenKiadó(List<Könyv> könyvLista)
    {
        Set<String> set = newHashSet("");
        for (Könyv könyv : könyvLista)
        {
            if (könyv.getKiadó() != null)
            {
                set.add(könyv.getKiadó().trim());
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
