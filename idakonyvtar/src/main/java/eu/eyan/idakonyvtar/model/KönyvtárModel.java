package eu.eyan.idakonyvtar.model;

import lombok.Getter;
import lombok.Setter;

import com.jgoodies.binding.list.SelectionInList;

public class KönyvtárModel
{
    @Getter
    private SelectionInList<Könyv> könyvek = new SelectionInList<Könyv>();

    @Getter
    @Setter
    private Könyvtár könyvtár;
}