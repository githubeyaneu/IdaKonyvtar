package eu.eyan.idakonyvtar.util;

import java.nio.charset.Charset;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import com.google.common.base.Charsets;

@RunWith(Theories.class)
public class BackupHelperTest
{
    @DataPoints
    public static Charset[] charsets = new Charset[]
    {
            Charsets.ISO_8859_1,
            Charsets.UTF_8,
            Charset.forName("Cp1252")
    };

    public static int cnt = 1;

    @Theory
    public void test(Charset charset1, Charset charset2)
    {
        // FIXME
//        String filename = "házilibrary.xls";
//        System.out.println(cnt + " " + charset1 + " " + charset2);
//        String to = new String(filename.getBytes(charset1));
//        BackupHelper.zipFile(new File("házilibrary.xls"), new File(cnt + "_" + to + ".zip"), charset2);
//        cnt++;
    }
}
