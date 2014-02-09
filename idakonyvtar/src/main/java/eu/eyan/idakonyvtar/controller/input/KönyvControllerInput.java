package eu.eyan.idakonyvtar.controller.input;

import java.util.List;

import eu.eyan.idakonyvtar.model.Könyv;

public class KönyvControllerInput
{
    public Könyv könyv;
    public List<Könyv> könyvLista;

    public KönyvControllerInput(Könyv könyv, List<Könyv> list)
    {
        this.könyv = könyv;
        this.könyvLista = list;
    }
}
