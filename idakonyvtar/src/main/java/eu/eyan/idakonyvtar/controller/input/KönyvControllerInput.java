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

    @Getter
    @Setter
    private List<String> oszlopok;

    @Getter
    private boolean isbnEnabled = false;

    @Getter
    private OszlopKonfiguráció oszlopKonfiguráció = null;

    @Getter
    private List<Könyv> könyvLista;

    private KönyvControllerInput()
    {
    }

    public static class Builder
    {
        private KönyvControllerInput input = new KönyvControllerInput();

        public Builder withKönyv(Könyv könyv)
        {
            this.input.könyv = könyv;
            return this;
        }

        public Builder withKönyvLista(List<Könyv> könyvLista)
        {
            this.input.könyvLista = könyvLista;
            return this;
        }

        public Builder withOszlopok(List<String> oszlopok)
        {
            this.input.oszlopok = oszlopok;
            return this;
        }

        public Builder withIsbnEnabled(boolean isbnEnabled)
        {
            this.input.isbnEnabled = isbnEnabled;
            return this;
        }

        public Builder withOszlopKonfiguráció(OszlopKonfiguráció oszlopKonfiguráció)
        {
            this.input.oszlopKonfiguráció = oszlopKonfiguráció;
            return this;
        }

        public KönyvControllerInput build()
        {
            if (this.input.könyv == null)
                throw new RuntimeException("A könyv nem lehet null");
            if (this.input.könyvLista == null)
                throw new RuntimeException("A könyvLista nem lehet null");
            if (this.input.oszlopok == null)
                throw new RuntimeException("A oszlopok nem lehet null");
            if (this.input.oszlopKonfiguráció == null)
                throw new RuntimeException("A oszlopKonfiguráció nem lehet null");
            return this.input;
        }

    }
}
