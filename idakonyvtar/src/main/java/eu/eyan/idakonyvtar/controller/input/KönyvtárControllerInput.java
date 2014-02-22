package eu.eyan.idakonyvtar.controller.input;

import java.io.File;

import lombok.Getter;

public class KönyvtárControllerInput
{
    @Getter
    private File file;

    public KönyvtárControllerInput(File file)
    {
        this.file = file;
    }
}
