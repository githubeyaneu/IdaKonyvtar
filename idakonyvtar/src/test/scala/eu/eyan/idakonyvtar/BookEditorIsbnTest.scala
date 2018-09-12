package eu.eyan.idakonyvtar


import java.awt.event.KeyEvent

import org.fest.swing.core.BasicRobot
import org.junit.After
import org.junit.Before
import org.junit.Test

import eu.eyan.idakonyvtar.controller.BookController
import eu.eyan.idakonyvtar.controller.input.BookControllerInput
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.ColumnConfigurations
import eu.eyan.idakonyvtar.model.ColumnKonfiguration
import eu.eyan.idakonyvtar.testhelper.BookEditorTestHelper
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.idakonyvtar.view.BookView
import eu.eyan.testutil.video.VideoRunner
import javax.swing.SwingUtilities


object BookEditorIsbnTest {

  def main(args: Array[String]): Unit = {
    val test: BookEditorIsbnTest = new BookEditorIsbnTest()
    test.setUp()
    test.testIsbnRead()
  }

}

class BookEditorIsbnTest extends AbstractUiTest {

  private var bookEditor: BookEditorTestHelper = _

  private var bookController: BookController = _

  @Before
  def setUp(): Unit = {
    val columns: List[String] =
      List("szimpla", "ac", "mm", "mmac", "cim")
    val book: Book =
      new Book.Builder(columns.size).withValue(0, "Érték1").build()
    val columnConfig: ColumnKonfiguration =
      new ColumnKonfiguration.Builder(4, columns.size + 1)
        .withRow("",
                 ColumnConfigurations.MULTIFIELD.name,
                 ColumnConfigurations.AUTOCOMPLETE.name,
                 ColumnConfigurations.MARC_CODE.name)
        .withRow(columns(0), "", "", "")
        .withRow(columns(1), "", "igen", "")
        .withRow(columns(2), "igen", "", "")
        .withRow(columns(3), "igen", "igen", "")
        .withRow(columns(4), "nem", "nem", "245-10-a")
        .build()
    val bookList: List[Book] = List(
      book,
      new Book.Builder(columns.size)
        .withValue(0, "Érték2")
        .withValue(1, "abc")
        .withValue(3, "abc")
        .build(),
      new Book.Builder(columns.size)
        .withValue(0, "Érték2")
        .withValue(1, "abd")
        .withValue(3, "abd")
        .build()
    )
    bookController = new BookController()
    val bookControllerInput: BookControllerInput = new BookControllerInput(
      book,
      columns,
      columnConfig,
      bookList,
      true,
      null)
    SwingUtilities.invokeLater(() =>
      DialogHelper.startModalDialog(null, bookController, bookControllerInput))
    bookEditor = new BookEditorTestHelper(
      BasicRobot.robotWithCurrentAwtHierarchy())
    VideoRunner.setComponentToRecord(bookEditor.getComponentToRecord)
  }
  /* FIXME images map*/

  /* FIXME images map*/

  @After
  def tearDown(): Unit = {
    bookEditor.cleanUp()
  }

  @Test
  def testIsbnRead(): Unit = {
    bookEditor.requireIsbnPresent()
    bookEditor.setNormalText(BookView.ISBN_TEXT, "9789631193701")
    bookEditor.keyboard(KeyEvent.VK_ENTER)
    bookEditor.requireNormalText("cim", "Abigél")
  }

}