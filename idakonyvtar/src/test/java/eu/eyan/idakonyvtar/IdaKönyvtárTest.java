package eu.eyan.idakonyvtar;

import java.io.File;

import org.junit.Test;

public class IdaKönyvtárTest
{
    @Test
    public void testImportExcel() throws Exception
    {
        new IdaKönyvtár().importExcel(new File("C:\\Users\\FA\\Desktop\\házikönyvtár.xls"));
    }
}
