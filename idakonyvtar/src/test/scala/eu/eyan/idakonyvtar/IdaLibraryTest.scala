package eu.eyan.idakonyvtar

import java.io.File

import org.fest.swing.core.matcher.JButtonMatcher
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import IdaLibraryTest.library
import eu.eyan.idakonyvtar.model.ColumnConfigurations
import eu.eyan.idakonyvtar.testhelper.IdaLibraryTestHelper
import eu.eyan.idakonyvtar.testhelper.LibraryFileBuilder
import eu.eyan.idakonyvtar.text.LanguageHandler
import eu.eyan.idakonyvtar.util.ExcelHandler
import eu.eyan.idakonyvtar.view.LibraryMenuAndToolBar
import eu.eyan.testutil.ExcelAssert
import eu.eyan.testutil.video.VideoRunner
import eu.eyan.util.swing.HighlightRenderer

object IdaLibraryTest {

  private var library: IdaLibraryTestHelper = new IdaLibraryTestHelper()
  
  def main(args: Array[String]) = {
    val t = new IdaLibraryTest()
    t.setUp()
  }

}

class IdaLibraryTest extends AbstractUiTest {

  @Before
  def setUp(): Unit = {
    LanguageHandler.saveSelectedLanguageInRegistry(classOf[IdaLibrary].getName)("English")
    library.start(null)
    VideoRunner.setComponentToRecord(library.getComponentToRecord)
  }

  @After
  def tearDown(): Unit = {
    library.cleanUp()
  }

  @Test
  def testStartProgram(): Unit = {
    library.requireVisible()
    library.checkTitleWithNumber(4)
  }

  @Test
  @Ignore
  def testMenu(): Unit = {
    library.clickMenu(LibraryMenuAndToolBar.ISBN_SEARCH)
    library.editor().clickCancel()
    library.clickMenu("Fájl")
  }

  @Test //  @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "test cleanup")
  def testLoadAndSave(): Unit = {
    val file: File = new LibraryFileBuilder()
      .withSheet(ExcelHandler.BOOKS)
      .withColumns("column1", "column2")
      .withRow("árvíztűrő tükörfúrógép", "ÁRVÍZTŰRŐ TÜKÖRFÚRÓGÉP")
      .withSheet(ExcelHandler.COLUMN_CONFIGURATION)
      .withColumns("", ColumnConfigurations.SHOW_IN_TABLE.name, "ko2")
      .withRow("column1", "igen", "")
      .withRow("column2 tükörfúrógép", "nem", "")
      .save()
    try {
      library.load(file)
      library.assertTableCell(1, 1, "árvíztűrő tükörfúrógép")
      library.checkTitleWithNumber(1)
    } finally file.delete()
    val file2: File = new File(System.currentTimeMillis() + ".xls")
    try {
      library.save(file2)
      ExcelAssert.assertExcelCell(
        file2,
        ExcelHandler.BOOKS,
        1,
        2,
        "árvíztűrő tükörfúrógép")
      library.checkTitleWithNumber(1)
    } finally file2.delete()
  }

  @Test
  def testSaveNewBook(): Unit = {
    library.assertTableCell(2, 1, "original title 1")
    library.clickNewButton()
    library.editor().requireIsbnPresent()
    library.editor().setNormalText("Cím", "New Title 1")
    library.editor().clickSave()
    library.assertTableCell(2, 1, "New Title 1")
    library.assertTableCell(2, 2, "original title 1")
    library.checkTitleWithNumber(5)
  }

  @Test
  def testNewBookSaveNot(): Unit = {
    library.assertTableCell(2, 1, "original title 1")
    library.clickNewButton()
    library.editor().setNormalText("Cím", "New Title 1")
    library.editor().clickCancel()
    library.assertTableCell(2, 1, "original title 1")
    library.checkTitleWithNumber(4)
  }

  @Test
  def testBookDeleteOk(): Unit = {
    library.assertTableCell(2, 1, "original title 1")
    library.requireDeleteDisabled()
    library.selectRow(1)
    library.requireDeleteEnabled()
    library.clickDeleteButton()
    library.clickApproveYes()
    library.assertTableCell(2, 1, "original title 2")
    library.requireDeleteDisabled()
    library.checkTitleWithNumber(3)
  }

  @Test
  def testBookDeleteCancel(): Unit = {
    library.assertTableCell(2, 1, "original title 1")
    library.requireDeleteDisabled()
    library.selectRow(1)
    library.requireDeleteEnabled()
    library.clickDeleteButton()
    library.clickApproveNo()
    library.assertTableCell(2, 1, "original title 1")
    library.requireDeleteEnabled()
    library.checkTitleWithNumber(4)
  }

  @Test
  def testFilter(): Unit = {
    library.filter("aron")
    library.assertTableCell(
      1,
      1,
      new StringBuilder("")
        .append(HighlightRenderer.HTML_START_TAG)
        .append("Tamási ")
        .append(HighlightRenderer.HIGHLIGHT_START_TAG)
        .append("Áron")
        .append(HighlightRenderer.HIGHLIGHT_END_TAG)
        .append(HighlightRenderer.HTML_END_TAG)
        .toString)
    library.assertTableCell(
      2,
      2,
      new StringBuilder("")
        .append(HighlightRenderer.HTML_START_TAG)
        .append("Kh")
        .append(HighlightRenderer.HIGHLIGHT_START_TAG)
        .append("áron")
        .append(HighlightRenderer.HIGHLIGHT_END_TAG)
        .append(" ladikján")
        .append(HighlightRenderer.HTML_END_TAG)
        .toString)
    library.assertTableCell(1, 2, "Illyés Gyula")
    library.assertTableRowCount(2)
  }

  @Test
  def testBookEditSave(): Unit = {
    library.assertTableCell(2, 1, "original title 1")
    library.doubleClick(1)
    library.editor().requireIsbnNotPresent()
    library.editor().setNormalText("Cím", "New Title 1")
    library.editor().clickSave()
    library.assertTableCell(2, 1, "New Title 1")
    library.checkTitleWithNumber(4)
  }

  @Test
  def testBookEditCancel(): Unit = {
    library.assertTableCell(2, 1, "original title 1")
    library.doubleClick(1)
    library.editor().setNormalText("Cím", "New Title")
    library.editor().clickCancel()
    library.assertTableCell(2, 1, "original title 1")
    library.checkTitleWithNumber(4)
  }

  @Test
  def testExitNo(): Unit = {
    library.requireVisible()
    library.clickExit()
    library.exitDialog().requireVisible()
    library.exitDialog().button(JButtonMatcher.withText("No")).click
    library.requireVisible()
  }

  @Test
  def testExitYes(): Unit = {
    library.requireVisible()
    library.clickExit()
    library.exitDialog().requireVisible()
    library.exitDialog().button(JButtonMatcher.withText("Yes")).click
    library.requireInvisible()
  }

}
