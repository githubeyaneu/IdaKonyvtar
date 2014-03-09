package eu.eyan.idakonyvtar.oszk;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.fest.assertions.Fail;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class OszkKeresoTest
{
    final static String ABIGEL_ISBN = "9789631193701";

    @Rule
    public Timeout globalTimeout = new Timeout(15000);

    @Test
    public void isbnKeresOszkban_muxik()
    {
        try
        {
            assertThat(OszkKereso.isbnKeresOszkban("9789631193701")).contains("Abigél");
//            assertThat(OszkKereso.isbnKeresOszkban("9789633708316")).contains("Királyok");
//            assertThat(OszkKereso.isbnKeresOszkban("9789632273822")).contains("Hallgatni");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Fail.fail(e.getMessage());
        }
    }

    @Test
    public void marc_parse_muxik() throws OszkKeresoException
    {
        List<Marc> abigel = OszkKereso.getMarcsToIsbn("9789631193701");
        assertThat(MarcHelper.findMarc(abigel, MarcCodes.CIM)).isEqualTo("Abigél");

        List<Marc> marai = OszkKereso.getMarcsToIsbn("9789632273822");
        assertThat(MarcHelper.findMarc(marai, MarcCodes.CIM)).isEqualTo("Hallgatni akartam");
    }
}
