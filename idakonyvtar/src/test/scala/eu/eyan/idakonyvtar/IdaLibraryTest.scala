package eu.eyan.idakonyvtar

import java.io.File

import org.fest.swing.core.matcher.JButtonMatcher
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import eu.eyan.idakonyvtar.testhelper.IdaLibraryTestHelper
import eu.eyan.idakonyvtar.testhelper.LibraryFileBuilder
import eu.eyan.idakonyvtar.text.TextsIda
import eu.eyan.idakonyvtar.util.LibraryExcelHandler
import eu.eyan.testutil.ExcelAssert
import eu.eyan.testutil.video.VideoRunner
import eu.eyan.util.swing.HighlightRenderer
import org.fest.swing.fixture.FrameFixture
import eu.eyan.log.LogWindow
import eu.eyan.util.awt.clipboard.ClipboardPlus
import eu.eyan.log.Log
import eu.eyan.util.registry.RegistryPlus
import java.awt.Desktop
import java.awt.peer.DesktopPeer
import org.mockito.Mockito
import org.mockito.ArgumentCaptor
import java.net.URI
import java.awt.Desktop.Action
import org.fest.swing.core.BasicRobot
import eu.eyan.util.scala.Try
import eu.eyan.idakonyvtar.testhelper.AbstractUiTest

object IdaLibraryTest {
  def main(args: Array[String]) = {
    val t = new IdaLibraryTest()
    t.setUp
  }
}

class IdaLibraryTest extends AbstractUiTest {
  private var library = new IdaLibraryTestHelper

  @Before
  def setUp = {
    Try(RegistryPlus.clear(classOf[IdaLibrary].getName))
    RegistryPlus.write(classOf[IdaLibrary].getName, classOf[TextsIda].getName, "English")
    library.start.toFront
    VideoRunner.setComponentToRecord(library.getComponentToRecord)
  }

  @After
  def tearDown = library.cleanUp

  @Test
  def testStartProgram = {
    library.requireVisible()
    library.checkTitleWithNumber(2)
  }

  @Test //  @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "test cleanup")
  def testLoadAndSaveAs(): Unit = {
    val file = new LibraryFileBuilder()
      .withSheet("books")
      .withColumns("column1", "column2")
      .withRow("árvíztűrő tükörfúrógép", "ÁRVÍZTŰRŐ TÜKÖRFÚRÓGÉP")
      .withSheet("config")
      .withColumns("", "mm", "ac", "marc", "intable", "rem" , "pict")
      .withRow("column1","","","", "igen", "")
      .withRow("column2 tükörfúrógép","","","", "nem", "")
      .save
    try {
      library.load(file)
      library.assertTableCell(1, 1, "árvíztűrő tükörfúrógép")
      library.checkTitleWithNumber(1)
    } finally file.delete()
    
    val file2 = new File(System.currentTimeMillis() + ".xls")
    
    try {
      library.saveAs(file2)
      ExcelAssert.assertExcelCell(
        file2,
        "books",
        1,
        2,
        "árvíztűrő tükörfúrógép")
      library.checkTitleWithNumber(1)
    } finally file2.delete
  }

  @Test
  def testSaveNewBook = {
    library.assertTableCell(2, 1, "original title 1")
    library.clickNewButton
    library.editor.requireIsbnPresent
    library.editor.setNormalText("Title", "New Title 1")
    library.editor.clickSave
    library.assertTableCell(2, 1, "New Title 1")
    library.assertTableCell(2, 2, "original title 1")
    library.checkTitleWithNumber(3)
  }

  @Test
  def testNewBookSaveNot = {
    library.assertTableCell(2, 1, "original title 1")
    library.clickNewButton
    library.editor.setNormalText("Title", "New Title 1")
    library.editor.clickCancel()
    library.assertTableCell(2, 1, "original title 1")
    library.checkTitleWithNumber(2)
  }

  @Test
  def testBookDeleteOk = {
    library.assertTableCell(2, 1, "original title 1")
    library.requireDeleteDisabled()
    library.selectRow(1)
    library.requireDeleteEnabled()
    library.clickDeleteButton()
    library.clickApproveYes()
    library.assertTableCell(2, 1, "Kháron ladikján")
    library.requireDeleteDisabled()
    library.checkTitleWithNumber(1)
  }

  @Test
  def testBookDeleteCancel = {
    library.assertTableCell(2, 1, "original title 1")
    library.requireDeleteDisabled()
    library.selectRow(1)
    library.requireDeleteEnabled()
    library.clickDeleteButton()
    library.clickApproveNo()
    library.assertTableCell(2, 1, "original title 1")
    library.requireDeleteEnabled()
    library.checkTitleWithNumber(2)
  }

  @Test
  def testFilter = {
    library.filter("aron")
    library.assertTableRowCount(1)
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
      1,
      new StringBuilder("")
        .append(HighlightRenderer.HTML_START_TAG)
        .append("Kh")
        .append(HighlightRenderer.HIGHLIGHT_START_TAG)
        .append("áron")
        .append(HighlightRenderer.HIGHLIGHT_END_TAG)
        .append(" ladikján")
        .append(HighlightRenderer.HTML_END_TAG)
        .toString)
  }

  @Test
  def testBookEditSave = {
    library.assertTableCell(2, 1, "original title 1")
    library.doubleClick(1)
    library.editor.requireIsbnNotPresent
    library.editor.setNormalText("Title", "New Title 1")
    library.editor.clickSave
    library.assertTableCell(2, 1, "New Title 1")
    library.checkTitleWithNumber(2)
  }

  @Test
  def testBookEditCancel = {
    library.assertTableCell(2, 1, "original title 1")
    library.doubleClick(1)
    library.editor.setNormalText("Title", "New Title")
    library.editor.clickCancel()
    library.assertTableCell(2, 1, "original title 1")
    library.checkTitleWithNumber(2)
  }

  @Test
  def testExitNo = {
    library.requireVisible()
    library.clickExit
    library.dialog.requireVisible()
    library.dialog.button(JButtonMatcher.withText("Cancel")).click
    library.requireVisible()
  }

  @Test
  def exitYes = {
    library.requireVisible
    library.menuItem("File", "Quit").click
    library.dialog.requireVisible
    library.dialog.component.getTitle ==> "Confirm"
    library.dialog.optionPane.requireMessage("Are you sure to quit?")
    library.dialog.button(JButtonMatcher.withText("Cancel")).requireText("Cancel") // :)
    library.dialog.button(JButtonMatcher.withText("Yes")).requireText("Yes") // :)
    library.dialog.button(JButtonMatcher.withText("Yes")).click
    library.requireNotExists
  }

  @Test
  def menu = {
    library.requireMenuText("File", "File")
    library.requireMenuText("Load", "File", "Load")
    library.requireMenuText("Save", "File", "Save")
    library.requireMenuText("Quit", "File", "Quit")
    library.requireMenuText("Language", "Language")
    library.requireMenuText("Magyar", "Language", "Magyar")
    library.requireMenuText("English", "Language", "English")
    library.requireMenuText("Deutsch", "Language", "Deutsch")
    library.requireMenuText("Debug", "Debug")
    library.requireMenuText("Log window", "Debug", "Log window")
    library.requireMenuText("Copy logs to clipboard", "Debug", "Copy logs to clipboard")
    library.requireMenuText("Clear registry", "Debug", "Clear registry")
    library.requireMenuText("Help", "Help")
    library.requireMenuText("Write email", "Help", "Write email")
    library.requireMenuText("About", "Help", "About")
  }

  @Test
  def logWindow = {
    library.menuItem("Debug", "Log window").click
    val logWindow = new FrameFixture(library.robot, classOf[LogWindow].getName)
    logWindow.requireVisible
  }

  @Test
  def copyLogs = {
    Log.activateInfoLevel.info("loginfo_x2z")
    library.menuItem("Debug", "Copy logs to clipboard").click
    library.dialog.requireVisible
    library.dialog.component.getTitle ==> "Copy logs to clipboard"
    library.dialog.optionPane.requireMessage("Please check the copied text. It could contain personal data as the infos of books etc…")
    library.dialog.button(JButtonMatcher.withText("O.K.")).requireText("O.K.") // :)
    library.dialog.button(JButtonMatcher.withText("O.K.")).click
    println("--------------------------")
    println(ClipboardPlus.getTextFromClipboard)
    println("--------------------------")
    ClipboardPlus.getTextFromClipboard.contains("loginfo_x2z") ==> true
  }

  @Test
  def clearRegistry = {
    RegistryPlus.readOption(classOf[IdaLibrary].getName, classOf[TextsIda].getName).get ==> "English"
    RegistryPlus.write(classOf[IdaLibrary].getName, "test", "xyz")
    library.menuItem("Debug", "Clear registry").click
    RegistryPlus.readOption(classOf[IdaLibrary].getName, classOf[TextsIda].getName) ==> None
    RegistryPlus.readOption(classOf[IdaLibrary].getName, "test") ==> None
  }

  @Test
  def about = {
    library.menuItem("Help", "About").click
    library.dialog.requireVisible
    library.dialog.component.getTitle ==> "About"
    library.dialog.optionPane.requireMessage("Personal use only. The creator does not take any responsibility to the damages of using this application. The application might use external sources to get data, the user is responsible for the consequences.")
    library.dialog.button(JButtonMatcher.withText("Close")).requireText("Close") // :)
    library.dialog.button(JButtonMatcher.withText("Close")).click
  }

  @Test
  def changeLanguage = {
    library.requireTitle("IdaLibrary - 2 books")
    library.menuItem("Language", "Deutsch").click
    library.requireTitle("IdaBibliothek - 2 Bücher")
    library.cleanUp
    library.start
    library.frame.target.getTitle ==>  "IdaBibliothek - 2 Bücher"
    library.menuItem("Sprachen", "English").click
    library.requireTitle("IdaLibrary - 2 books")
  }

  @Test
  def writeEmail = {
    // Given
    Log.info("write EMAIL+?")
    val peerMock = mock[DesktopPeer]
    peerMock.isSupported(Action.MAIL) --> true
    Desktop.getDesktop.setFieldValue("peer", peerMock)

    // When
    library.menuItem("Help", "Write email").click
    library.dialog.requireVisible
    library.dialog.component.getTitle ==> "Copy logs to clipboard"
    library.dialog.optionPane.requireMessage("Please check the copied text. It could contain personal data as the infos of books etc…")
    library.dialog.button(JButtonMatcher.withText("O.K.")).requireText("O.K.") // :)
    library.dialog.button(JButtonMatcher.withText("O.K.")).click

    // Then
    peerMock.verify.mail(capture[URI](_.toString shouldStartWith "mailto:idalibrary@eyan.hu?subject=IdaLibrary%20error&body="))
    peerMock.verify.mail(capture[URI](_.toString shouldContain "Write+email%29%0D%0A"))
  }
}