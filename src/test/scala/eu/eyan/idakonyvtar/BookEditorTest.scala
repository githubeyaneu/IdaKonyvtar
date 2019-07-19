package eu.eyan.idakonyvtar

import java.awt.event.KeyEvent

import eu.eyan.idakonyvtar.controller.BookEditor
import eu.eyan.idakonyvtar.model.{Autocomplete, Book, BookField, Multifield}
import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.idakonyvtar.testhelper.{AbstractUiTest, BookEditorTestHelper}
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.log.Log
import eu.eyan.testutil.video.VideoRunner
import eu.eyan.util.excel.ExcelColumn
import eu.eyan.util.text.Text
import javax.swing.SwingUtilities
import org.fest.assertions.Assertions.assertThat
import org.fest.swing.core.BasicRobot
import org.junit.{After, Before, Test}

import scala.collection.mutable

object BookEditorTest {
  def main(args: Array[String]): Unit = {
    new BookEditorTest().setUp()
  }

}

class BookBuilder(columnCount: Int) {
  val fields = (0 until columnCount).map(c => BookField(ExcelColumn(c), "", List(), Array())).toList
  val book = Book.empty(fields)
  val fieldsAndValues = mutable.Map[BookField, String]()

  def withValue(columnIndex: Int, value: String): BookBuilder = {
    Log.debug(columnIndex + " " + value)
    fieldsAndValues.put(fields(columnIndex), value)
    this
  }

  def build: Book = Book( (book.getValues ++ fieldsAndValues).toList)
}

class BookEditorTest extends AbstractUiTest {

  private var bookEditor: BookEditorTestHelper = _

  private var bookController: BookEditor = _

  private val fields = List(
    BookField(ExcelColumn(0), "szimpla", List(), Array()),
    BookField(ExcelColumn(1), "ac", List(Autocomplete), Array()),
    BookField(ExcelColumn(2), "mm", List(Multifield), Array()),
    BookField(ExcelColumn(3), "mmac", List(Multifield, Autocomplete), Array()),
    BookField(ExcelColumn(4), "cim", List(), Array(new Marc("245", "10", "a", ""))))

  private val inputBook: Book = new BookBuilder(5).withValue(0, "Érték1").build

  private def outputBook = bookController.getResult

  @Before
  def setUp(): Unit = {
    Log.activateInfoLevel
    val bookList: List[Book] = List(
      inputBook,
      new BookBuilder(5)
        .withValue(0, "Érték2")
        .withValue(1, "abc")
        .withValue(3, "abc")
        .build,
      new BookBuilder(5)
        .withValue(0, "Érték2")
        .withValue(1, "abd")
        .withValue(3, "abd")
        .build)
    bookController = BookEditor.editBookWithoutIsbn(inputBook, fields, bookList, null)
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
    assertThat(outputBook.getValue(fields.head)).isEqualTo("szimpla")
  }

  @Test
  def testAutocompleteDefault(): Unit = {
    bookEditor.setNormalText("ac", "a")
    bookEditor.keyboard(KeyEvent.VK_ESCAPE)
    bookEditor.clickSave()
    assertThat(outputBook.getValue(fields(1))).isEqualTo("a")
  }

  @Test
  def testAutocompleteNew(): Unit = {
    bookEditor.enterNormalText("ac", "a")
    bookEditor.keyboard(KeyEvent.VK_DELETE)
    bookEditor.keyboard(KeyEvent.VK_ESCAPE)
    bookEditor.clickSave()
    assertThat(outputBook.getValue(fields(1))).isEqualTo("a")
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
    assertThat(outputBook.getValue(fields(2))).isEqualTo("ab + c")
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
    assertThat(outputBook.getValue(fields(3))).isEqualTo("ab + c + a")
  }

  @Test
  def test2(): Unit = {
    bookEditor.clickSave
  }

}
