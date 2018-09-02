package eu.eyan.idakonyvtar

import java.awt.event.KeyEvent

import org.fest.assertions.Assertions.assertThat
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
import eu.eyan.log.Log
import eu.eyan.testutil.video.VideoRunner
import javax.swing.SwingUtilities

object BookEditorTest {

  def main(args: Array[String]): Unit = {
    new BookEditorTest().setUp()
  }

}

class BookEditorTest extends AbstractUiTest {

  private var bookEditor: BookEditorTestHelper = _

  private var bookController: BookController = _

  @Before
  def setUp(): Unit = {
    Log.activateInfoLevel
    val columns: List[String] = List("szimpla", "ac", "mm", "mmac")
    val book: Book =
      new Book.Builder(columns.size).withValue(0, "Érték1").build()
    val columnConfiguration: ColumnKonfiguration =
      new ColumnKonfiguration.Builder(3, columns.size + 1)
        .withRow(
          "",
          ColumnConfigurations.MULTIFIELD.name,
          ColumnConfigurations.AUTOCOMPLETE.name)
        .withRow(columns(0), "", "")
        .withRow(columns(1), "", "igen")
        .withRow(columns(2), "igen", "")
        .withRow(columns(3), "igen", "igen")
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
        .build())
    bookController = new BookController()
    val bookControllerInput: BookControllerInput = new BookControllerInput(
      book,
      columns,
      columnConfiguration,
      bookList,
      false,
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
  def testNormalField(): Unit = {
    bookEditor.setNormalText("szimpla", "szimpla")
    bookEditor.clickSave()
    assertThat(bookController.getOutput.getValue(0)).isEqualTo("szimpla")
  }

  @Test
  def testAutocompleteDefault(): Unit = {
    bookEditor.setNormalText("ac", "a")
    bookEditor.keyboard(KeyEvent.VK_ESCAPE)
    bookEditor.clickSave()
    assertThat(bookController.getOutput.getValue(1)).isEqualTo("a")
  }

  @Test
  def testAutocompleteNew(): Unit = {
    bookEditor.enterNormalText("ac", "a")
    bookEditor.keyboard(KeyEvent.VK_DELETE)
    bookEditor.keyboard(KeyEvent.VK_ESCAPE)
    bookEditor.clickSave()
    assertThat(bookController.getOutput.getValue(1)).isEqualTo("a")
  }

  @Test
  def testMultiField(): Unit = {
    bookEditor.setNormalText("mm1", "")
    bookEditor.enterNormalText("mm1", "a")
    bookEditor.enterNormalText("mm2", "b")
    bookEditor.enterNormalText("mm1", "b")
    bookEditor.enterNormalText("mm3", "c")
    bookEditor.multifieldDelete("mm", 2)
    bookEditor.requireDeleteDisabled("mm", 4)
    bookEditor.clickSave
    assertThat(bookController.getOutput.getValue(2)).isEqualTo("ab + c")
  }

  @Test
  def testMultiFieldAutoComplete(): Unit = {
    bookEditor.setNormalText("mmac1", "")
    bookEditor.enterNormalText("mmac1", "a")
    bookEditor.autocomplete("mmac1").pressEscape
    bookEditor.enterNormalText("mmac2", "b")
    bookEditor.autocomplete("mmac2").pressEscape
    bookEditor.enterNormalText("mmac1", "b")
    bookEditor.autocomplete("mmac1").pressEscape
    bookEditor.enterNormalText("mmac3", "c")
    bookEditor.autocomplete("mmac3").pressEscape
    bookEditor.multifieldDelete("mmac", 2)
    bookEditor.requireDeleteDisabled("mmac", 4)
    bookEditor.enterNormalText("mmac4", "a")
    bookEditor.autocomplete("mmac4").pressEscape
    bookEditor.clickSave
    assertThat(bookController.getOutput.getValue(3)).isEqualTo("ab + c + a")
  }

  @Test
  def test2(): Unit = {
    bookEditor.clickSave
  }

}
