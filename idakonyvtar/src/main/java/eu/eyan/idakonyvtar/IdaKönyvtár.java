package eu.eyan.idakonyvtar;

import java.io.File;

import eu.eyan.idakonyvtar.controller.KönyvtárController;
import eu.eyan.idakonyvtar.controller.input.KönyvtárControllerInput;
import eu.eyan.idakonyvtar.util.DialogHelper;

public class IdaKönyvtár
{
    public static void main(final String[] args)
    {
        DialogHelper.runInFrameFullScreen(new KönyvtárController(), new KönyvtárControllerInput(new File("házikönyvtár.xls")));
    }
}
