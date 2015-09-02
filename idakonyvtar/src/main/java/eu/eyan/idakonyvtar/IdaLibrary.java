package eu.eyan.idakonyvtar;

import java.io.File;
import java.net.URL;

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

        File fileToOpen = new File(pathname);
        if (!fileToOpen.exists())
        {
            URL resource = Resources.getResource(pathname);
            fileToOpen = new File(resource.getFile());
        }
        System.out.println("Resource -> File: " + fileToOpen);
        DialogHelper.runInFrameFullScreen(new LibraryController(), new LibraryControllerInput(fileToOpen), LibraryController.TITLE);
    }
}
