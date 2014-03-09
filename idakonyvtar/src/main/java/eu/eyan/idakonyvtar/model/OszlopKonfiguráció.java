package eu.eyan.idakonyvtar.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import eu.eyan.idakonyvtar.oszk.Marc;

//FIXME: Refactor, mert ez így érthetelen állandó fájdalom
public class OszlopKonfiguráció
{
    public static enum OszlopKonfigurációk
    {
        MULTIMEZŐ("MultiMező"),
        AUTOCOMPLETE("AutoComplete"),
        MARCKÓD("MarcKód"),
        EMLÉKEZŐ("Emlékező"),
        MEGJELENÍTÉS_TÁBLÁZATBAN("Táblázatban");

        @Getter
        private String név;

        OszlopKonfigurációk(String konfigurációNév)
        {
            this.név = konfigurációNév;
        }

    }

    @Getter
    @Setter
    private String[][] tábla;

    public boolean isIgen(String oszlopNév, OszlopKonfigurációk oszlopKonfiguráció)
    {
        return getÉrték(oszlopNév, oszlopKonfiguráció).equalsIgnoreCase("Igen");
    }

    private String getÉrték(String oszlopNév, OszlopKonfigurációk oszlopKonfiguráció)
    {
        int oszlopIndex = getOszlopIndex(oszlopNév);
        int konfigurációIndex = getKonfigurációIndex(oszlopKonfiguráció);
        if (oszlopIndex > 0 && konfigurációIndex > 0)
        {
            return tábla[konfigurációIndex][oszlopIndex];
        }
        return "";
    }

    private int getOszlopIndex(String oszlopNév)
    {
        if (tábla.length > 0)
        {
            for (int konfigurációIndex = 0; konfigurációIndex < tábla[0].length; konfigurációIndex++)
            {
                if (tábla[0][konfigurációIndex].equalsIgnoreCase(oszlopNév))
                {
                    return konfigurációIndex;
                }
            }
        }
        return -1;
    }

    private int getKonfigurációIndex(OszlopKonfigurációk konfigurációNév)
    {
        for (int oszlopIndex = 0; oszlopIndex < tábla.length; oszlopIndex++)
        {
            if (tábla[oszlopIndex][0].equalsIgnoreCase(konfigurációNév.getNév()))
            {
                return oszlopIndex;
            }
        }
        return -1;
    }

    public List<Marc> getMarcKódok(String oszlopNév) throws Exception
    {
        ArrayList<Marc> ret = newArrayList();
        try
        {
            String[] marcKódSzövegek = getÉrték(oszlopNév, OszlopKonfigurációk.MARCKÓD).split(",");
            for (String string : marcKódSzövegek)
            {
                String[] kódok = string.split("-");
                if (kódok.length > 2)
                {
                    ret.add(new Marc(kódok[0], kódok[1], kódok[2], null));
                }
            }
        }
        catch (Exception e)
        {
            throw new Exception("A Marc kódot nem lehet a konfigurációból beolvasni: " + oszlopNév);
        }
        return ret;
    }

    public List<String> getEmlékezőOszlopok()
    {
        List<String> emlékezőOszlopok = newArrayList();
        for (int oszlopIndex = 1; oszlopIndex < tábla[0].length; oszlopIndex++)
        {
            if (isIgen(tábla[0][oszlopIndex], OszlopKonfigurációk.EMLÉKEZŐ))
            {
                emlékezőOszlopok.add(tábla[0][oszlopIndex]);
            }
        }
        return emlékezőOszlopok;
    }

    public static class Builder
    {
        public Builder(int columns, int rows)
        {
            this.oszlopKonfiguráció.setTábla(new String[columns][rows]);
            this.actualRow = 0;
        }

        private int actualRow;
        private OszlopKonfiguráció oszlopKonfiguráció = new OszlopKonfiguráció();

        // FIXME phüjj...
        public Builder withRow(String... values)
        {
            for (int i = 0; i < values.length; i++)
            {
                this.oszlopKonfiguráció.getTábla()[i][actualRow] = values[i];
            }
            actualRow++;
            return this;
        }

        public OszlopKonfiguráció build()
        {
            return this.oszlopKonfiguráció;
        }
    }
}
