package eu.eyan.idakonyvtar.controller.input;

import java.io.File;

import lombok.Getter;

public class LibraryControllerInput
{
    @Getter
    private File file;

    public LibraryControllerInput(File file)
    {
        this.file = file;
    }
}
