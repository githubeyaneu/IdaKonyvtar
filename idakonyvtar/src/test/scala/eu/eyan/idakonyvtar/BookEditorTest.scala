package eu.eyan.idakonyvtar

import java.awt.event.KeyEvent

import org.fest.assertions.Assertions.assertThat
import org.fest.swing.core.BasicRobot
import org.junit.After
import org.junit.Before
import org.junit.Test

import eu.eyan.idakonyvtar.controller.BookController
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.testhelper.BookEditorTestHelper
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.log.Log
import eu.eyan.testutil.video.VideoRunner
import javax.swing.SwingUtilities
import eu.eyan.util.text.Text
import eu.eyan.idakonyvtar.text.TechnicalTextsIda
import scala.annotation.varargs
import eu.eyan.idakonyvtar.model.BookField
import eu.eyan.idakonyvtar.testhelper.AbstractUiTest
import eu.eyan.idakonyvtar.controller.NO_ISBN
import org.apache.velocity.tools.generic.FieldTool.MutableField
import eu.eyan.idakonyvtar.util.ExcelHandler.FieldConfiguration
import eu.eyan.util.excel.ExcelSheet
import eu.eyan.util.excel.ExcelColumn
import eu.eyan.util.excel.ExcelRow

object BookEditorTest {
  def main(args: Array[String]): Unit = {
    new BookEditorTest().setUp()
  }

}
class FieldConfigurationBuilder(columnCount: Int, rowCount: Int) {
  @varargs def withRow(values: String*): FieldConfigurationBuilder = {
    for { columnIndex <- 0 until values.length } table.put((ExcelColumn(columnIndex), ExcelRow(actualRow)), values(columnIndex))
    actualRow = actualRow + 1
    this
  }

  private val columns = for (columnIndex <- 0 until columnCount) yield ExcelColumn(columnIndex)
  private val rows = for (rowIndex <- 0 until rowCount) yield ExcelRow(rowIndex)
  private val table = scala.collection.mutable.Map[(ExcelColumn, ExcelRow), String]()
  private var actualRow: Int = 0

  def build() = new FieldConfiguration(new ExcelSheet(columns, rows, table.toMap))
}

class BookBuilder(columnCount: Int) {
    val fields = (0 until columnCount).toSeq.map(c => BookField(ExcelColumn(c),"", List(/*FIXME*/), Array())).toList
    val book = Book.empty(fields)

    def withValue(columnIndex: Int, value: String): BookBuilder = {
      Log.debug(columnIndex + " " + value)
      book.setValue(fields(columnIndex))(value)
      this
    }

    def build(): Book = book
  }

class BookEditorTest extends AbstractUiTest {

  private var bookEditor: BookEditorTestHelper = _

  private var bookController: BookController = _

  private val columns: List[String] = List("szimpla", "ac", "mm", "mmac")
  private val fields = columns.zipWithIndex.map(t=>BookField(ExcelColumn(t._2),t._1, List(/*FIXME*/), Array()))

  private val book: Book = new BookBuilder(columns.size).withValue(0, "Érték1").build()

  @Before
  def setUp(): Unit = {
    Log.activateInfoLevel
    val columnConfiguration: FieldConfiguration =
      new FieldConfigurationBuilder(3, columns.size + 1)
        .withRow(
          "",
          "multi",
          "autocomplete")
        .withRow(columns(0), "", "")
        .withRow(columns(1), "", "igen")
        .withRow(columns(2), "igen", "")
        .withRow(columns(3), "igen", "igen")
        .build()
    val bookList: List[Book] = List(
      book,
      new BookBuilder(columns.size)
        .withValue(0, "Érték2")
        .withValue(1, "abc")
        .withValue(3, "abc")
        .build(),
      new BookBuilder(columns.size)
        .withValue(0, "Érték2")
        .withValue(1, "abd")
        .withValue(3, "abd")
        .build())
    bookController = new BookController(
      book,
      fields,
      bookList,
      NO_ISBN,
      null)
    SwingUtilities.invokeLater(() =>
      DialogHelper.yesNoEditor(null, bookController.getComponent, new Text("title"), new Text("save"), new Text("cancel")))
    bookEditor = new BookEditorTestHelper(
      BasicRobot.robotWithCurrentAwtHierarchy())
    VideoRunner.setComponentToRecord(bookEditor.getComponentToRecord)
  }

  @After
  def tearDown(): Unit = {
    bookEditor.cleanUp()
  }

  @Test
  def testNormalField(): Unit = {
    bookEditor.setNormalText("szimpla", "szimpla")
    bookEditor.clickSave()
    assertThat(book.getValue(fields(0))).isEqualTo("szimpla")
  }

  @Test
  def testAutocompleteDefault(): Unit = {
    bookEditor.setNormalText("ac", "a")
    bookEditor.keyboard(KeyEvent.VK_ESCAPE)
    bookEditor.clickSave()
    assertThat(book.getValue(fields(1))).isEqualTo("a")
  }

  @Test
  def testAutocompleteNew(): Unit = {
    bookEditor.enterNormalText("ac", "a")
    bookEditor.keyboard(KeyEvent.VK_DELETE)
    bookEditor.keyboard(KeyEvent.VK_ESCAPE)
    bookEditor.clickSave()
    assertThat(book.getValue(fields(1))).isEqualTo("a")
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
    assertThat(book.getValue(fields(2))).isEqualTo("ab + c")
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
    assertThat(book.getValue(fields(3))).isEqualTo("ab + c + a")
  }

  @Test
  def test2(): Unit = {
    bookEditor.clickSave
  }

}
