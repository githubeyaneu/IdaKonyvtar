package eu.eyan.idakonyvtar.controller.input;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import eu.eyan.idakonyvtar.model.Könyv;

public class KönyvControllerInput
{
    @Getter
    @Setter
    private Könyv könyv;

    // FIXME a lista nem ide tartozik, csak setek kellenek az autocompletehez
    @Getter
    @Setter
    private List<Könyv> könyvLista;

    @Getter
    @Setter
    private List<String> oszlopok;

    public KönyvControllerInput(Könyv könyv, List<Könyv> list, List<String> oszlopok)
    {
        this.könyv = könyv;
        this.könyvLista = list;
        this.oszlopok = oszlopok;
    }
}
