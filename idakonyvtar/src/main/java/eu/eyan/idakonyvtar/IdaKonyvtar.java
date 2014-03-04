package eu.eyan.idakonyvtar;

import java.io.File;

import eu.eyan.idakonyvtar.controller.KönyvtárController;
import eu.eyan.idakonyvtar.controller.input.KönyvtárControllerInput;
import eu.eyan.idakonyvtar.util.DialogHelper;

public class IdaKonyvtar
{
    private static final String ALAPÉRTELMEZETT_KÖNYVTÁRFILE = "házikönyvtár.xls";

    public static void main(final String[] args)
    {
        String pathname = ALAPÉRTELMEZETT_KÖNYVTÁRFILE;
        if (args != null && args.length > 0 && args[0] != null)
        {
            pathname = args[0];
        }
        DialogHelper.runInFrameFullScreen(new KönyvtárController(), new KönyvtárControllerInput(new File(pathname)));
    }
}
