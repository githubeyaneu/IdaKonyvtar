package eu.eyan.idakonyvtar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.io.Closeables;

public class BackupHelper {

    public static void zipFile(File inputFile, File to) {
        byte[] buffer = new byte[1024];
        ZipOutputStream zipOutputStream = null;
        FileInputStream fileInputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(to));
            zipOutputStream.putNextEntry(new ZipEntry(inputFile.getName()));
            fileInputStream = new FileInputStream(inputFile);
            int len;
            while ((len = fileInputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, len);
            }
            zipOutputStream.closeEntry();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            Closeables.closeQuietly(zipOutputStream);
            Closeables.closeQuietly(fileInputStream);
        }
    }

    public static void backup(File file) {
        // TODO Auto-generated method stub

    }
}