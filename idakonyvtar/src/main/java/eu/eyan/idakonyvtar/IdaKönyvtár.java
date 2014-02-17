package eu.eyan.idakonyvtar;

import java.io.File;

import eu.eyan.idakonyvtar.controller.KönyvtárController;
import eu.eyan.idakonyvtar.controller.input.KönyvtárControllerInput;
import eu.eyan.idakonyvtar.util.DialogHandler;

public class IdaKönyvtár
{
    public static void main(final String[] args)
    {
        DialogHandler.runInFrame(new KönyvtárController(), new KönyvtárControllerInput(new File("házikönyvtár.xls")));
    }
}
