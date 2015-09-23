package eu.eyan.idakonyvtar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.io.Closeables;

object BackupHelper {
  def zipFile(inputFile: File, to: File) =
    {
      val buffer = Array.ofDim[Byte](1024)
      var zipOutputStream: ZipOutputStream = null
      var fileInputStream: FileInputStream = null
      // FIXME fájlnév ékezet probléma a zipben
      try {
        zipOutputStream = new ZipOutputStream(new FileOutputStream(to))
        // FIXME: mindig ez a név...
        zipOutputStream.putNextEntry(new ZipEntry("backup.xls"))
        fileInputStream = new FileInputStream(inputFile)
        var len = fileInputStream.read(buffer)
        while (len > 0) {
          zipOutputStream.write(buffer, 0, len)
          len = fileInputStream.read(buffer)
        }
        zipOutputStream.closeEntry()
      } catch {
        case ex: IOException => ex.printStackTrace()
      } finally {
        Closeables.closeQuietly(zipOutputStream);
        Closeables.closeQuietly(fileInputStream);
      }
    }
}