package eu.eyan.idakonyvtar;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

import org.fest.assertions.Fail;
import org.junit.Test;

import eu.eyan.idakonyvtar.oszk.OszkKereso;

public class OszkKeresoTest
{
    final static String ABIGEL_ISBN = "9789631193701";

    @Test
    public void isbnKeresOszkban_muxik()
    {
        try
        {
            assertThat(OszkKereso.isbnKeresOszkban("9789631193701")).contains("Abigél");
            assertThat(OszkKereso.isbnKeresOszkban("9789633708316")).contains("Királyok");
            assertThat(OszkKereso.isbnKeresOszkban("9789632273822")).contains("Hallgatni");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Fail.fail(e.getMessage());
        }
    }
}
