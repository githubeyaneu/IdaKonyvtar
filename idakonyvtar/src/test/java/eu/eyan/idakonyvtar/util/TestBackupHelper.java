package eu.eyan.idakonyvtar.util;

import java.io.File;

import org.junit.Test;

import com.google.common.io.Resources;

public class TestBackupHelper
{

    @Test
    public void testZipFile() throws Exception
    {
        BackupHelper.zipFile(new File(Resources.getResource("test.xls").getFile()), new File("zip.zip"));
    }

    @Test
    public void testBackup() throws Exception
    {
        // TODO
//        BackupHelper.backup(new File(Resources.getResource("test.xls").getFile()));
    }
}
