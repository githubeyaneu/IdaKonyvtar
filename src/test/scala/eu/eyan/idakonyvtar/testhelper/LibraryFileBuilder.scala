package eu.eyan.idakonyvtar.testhelper

import java.io.{File, IOException}

import eu.eyan.util.excel.ExcelPlus
import jxl.Workbook
import jxl.write.{Label, WritableSheet, WritableWorkbook}

class LibraryFileBuilder {

  private var workbook: WritableWorkbook = _

  private var sorok = 0

  private var actualSheet: WritableSheet = _

  var file: File = new File(System.currentTimeMillis() + ".xls")

  try workbook = Workbook.createWorkbook(file, ExcelPlus.WORKBOOK_SETTINGS)
  catch { case e: IOException => e.printStackTrace() }

  def withSheet(sheetName: String): LibraryFileBuilder = {
    sorok = 0
    actualSheet = workbook.createSheet(sheetName, workbook.getNumberOfSheets)
    this
  }
    
  def withColumns(columns: String*): LibraryFileBuilder = {
    withRow(columns: _*)
    this
  }

  def withRow(row: String*): LibraryFileBuilder = {
    
    for (i <- 0 until row.length) {
      try {
        actualSheet.addCell(new Label(i, sorok, row(i)))
      }
      catch {
        case e: Exception => e.printStackTrace()

      }
    }
    sorok += 1
    this
  }

  def save(): File = {
    try {
      workbook.write()
      workbook.close()
    } 
    catch { case e: Exception => e.printStackTrace() 
    }
    file
  }
}