package eu.eyan.idakonyvtar.oszk;

import java.util.List;

public class MarcHelper
{

    public static String findMarc(List<Marc> marcs, MarcCodes marcCode)
    {
        for (Marc marc : marcs)
        {
            if (marcCode.getMarc1().equals(marc.getMarc1())
                    && marcCode.getMarc2().equals(marc.getMarc2())
                    && marcCode.getMarc2().equals(marc.getMarc2()))
            {
                return marc.getValue();
            }
        }
        return null;
    }

}
