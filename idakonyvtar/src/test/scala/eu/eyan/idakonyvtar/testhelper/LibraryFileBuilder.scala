package eu.eyan.idakonyvtar.testhelper

import java.io.File
import java.io.IOException

import scala.collection.mutable.MutableList

import eu.eyan.idakonyvtar.util.ExcelHandler
import jxl.Workbook
import jxl.write.Label
import jxl.write.WritableSheet
import jxl.write.WritableWorkbook

class LibraryFileBuilder {

  private var workbook: WritableWorkbook = null

  private var sorok: MutableList[Array[String]] = MutableList()

  private var actualSheet: WritableSheet = null

  var file: File = new File(System.currentTimeMillis() + ".xls")

  try workbook =
    Workbook.createWorkbook(file, ExcelHandler.WORKBOOK_SETTINGS)
  catch {
    case e: IOException => e.printStackTrace()

  }

  def withColumns(columns: String*): LibraryFileBuilder = {
    for (i <- 0 until columns.length) {
      try actualSheet.addCell(new Label(i, 0, columns(i)))
      catch {
        case e: Exception => e.printStackTrace()

      }
    }
    this
  }

  def withRow(row: String*): LibraryFileBuilder = {
    sorok += row.toArray
    for (i <- 0 until row.length) {
      try actualSheet.addCell(new Label(i, sorok.size, row(i)))
      catch {
        case e: Exception => e.printStackTrace()

      }
    }
    this
  }

  def save(): File = {
    try {
      workbook.write()
      workbook.close()
    } catch {
      case e: Exception => e.printStackTrace()

    }
    file
  }

  def withSheet(sheetName: String): LibraryFileBuilder = {
    actualSheet = workbook.createSheet(sheetName, workbook.getNumberOfSheets)
    this
  }

}
