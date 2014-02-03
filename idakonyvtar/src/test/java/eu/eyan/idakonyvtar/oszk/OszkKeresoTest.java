package eu.eyan.idakonyvtar.oszk;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.fest.assertions.Fail;
import org.junit.Test;

public class OszkKeresoTest
{
    final static String ABIGEL_ISBN = "9789631193701";

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
    public void mar_parse_muxik()
    {
        List<Marc> abigel = OszkKereso.getMarcsToIsbn("9789631193701");
        assertThat(MarcHelper.findMarc(abigel, Marcs.CIM)).isEqualTo("Abigél");

        List<Marc> marai = OszkKereso.getMarcsToIsbn("9789632273822");
        assertThat(MarcHelper.findMarc(marai, Marcs.CIM)).isEqualTo("Hallgatni akartam");
    }

    public static void main(String[] args)
    {
        new OszkKeresoTest().mar_parse_muxik();
    }
}
