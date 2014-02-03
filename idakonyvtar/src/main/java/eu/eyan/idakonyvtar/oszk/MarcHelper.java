package eu.eyan.idakonyvtar.oszk;

import java.util.List;

public class MarcHelper
{

    public static String findMarc(List<Marc> marcs, Marcs cim)
    {
        for (Marc marc : marcs)
        {
            if (cim.getMarc1().equals(marc.getMarc1())
                    && cim.getMarc2().equals(marc.getMarc2())
                    && cim.getMarc2().equals(marc.getMarc2()))
            {
                return marc.getValue();
            }
        }
        return null;
    }

}
