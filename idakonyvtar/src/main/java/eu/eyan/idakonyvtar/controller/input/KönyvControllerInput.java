package eu.eyan.idakonyvtar.controller.input;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.model.OszlopKonfiguráció;

public class KönyvControllerInput
{
    public final static boolean ISBN_ENABLED = true;

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

    @Getter
    private boolean isbnEnabled;

    @Getter
    private OszlopKonfiguráció oszlopKonfiguráció;

    public KönyvControllerInput(Könyv könyv, List<Könyv> list, List<String> oszlopok)
    {
        this(könyv, list, oszlopok, false, null);
    }

    public KönyvControllerInput(Könyv könyv, List<Könyv> list, List<String> oszlopok, boolean isbnEnabled, OszlopKonfiguráció oszlopKonfiguráció)
    {
        this.könyv = könyv;
        this.könyvLista = list;
        this.oszlopok = oszlopok;
        this.isbnEnabled = isbnEnabled;
        this.oszlopKonfiguráció = oszlopKonfiguráció;
    }
}
