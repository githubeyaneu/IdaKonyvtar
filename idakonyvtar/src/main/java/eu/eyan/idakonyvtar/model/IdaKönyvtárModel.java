package eu.eyan.idakonyvtar.model;

import lombok.Getter;

import com.jgoodies.binding.list.SelectionInList;

public class IdaKönyvtárModel
{
    @Getter
    private SelectionInList<Könyv> könyvek = new SelectionInList<Könyv>();
}