package eu.eyan.idakonyvtar;

import java.io.File;

import com.google.common.io.Resources;

import eu.eyan.idakonyvtar.controller.LibraryController;
import eu.eyan.idakonyvtar.controller.input.LibraryControllerInput;
import eu.eyan.idakonyvtar.util.DialogHelper;

public class IdaLibrary
{
    private static final String DEFAULT_LIBRARY = "library.xls";

    public static void main(final String[] args)
    {
        String pathname = DEFAULT_LIBRARY;
        if (args != null && args.length > 0 && args[0] != null)
        {
            pathname = args[0];
        }
        DialogHelper.runInFrameFullScreen(new LibraryController(), new LibraryControllerInput(new File(Resources.getResource(pathname).getFile())), LibraryController.TITLE);
    }
}
