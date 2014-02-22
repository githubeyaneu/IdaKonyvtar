package eu.eyan.idakonyvtar.model;

import lombok.Getter;
import lombok.Setter;

public class OszlopKonfiguráció
{
    @Getter
    @Setter
    private String[][] tábla;

    public boolean isIgen(String oszlopNév, String konfigurációNév)
    {
        return getÉrték(oszlopNév, konfigurációNév).equalsIgnoreCase("Igen");
    }

    private String getÉrték(String oszlopNév, String konfigurációNév)
    {
        int oszlopIndex = getOszlopIndex(oszlopNév);
        int konfigurációIndex = getKonfigurációIndex(konfigurációNév);
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

    private int getKonfigurációIndex(String konfigurációNév)
    {
        for (int oszlopIndex = 0; oszlopIndex < tábla.length; oszlopIndex++)
        {
            if (tábla[oszlopIndex][0].equalsIgnoreCase(konfigurációNév))
            {
                return oszlopIndex;
            }
        }
        return -1;
    }
}
