package eu.eyan.idakonyvtar

import java.awt.event.KeyEvent

import org.fest.swing.core.BasicRobot
import org.junit.After
import org.junit.Before
import org.junit.Test

import eu.eyan.idakonyvtar.controller.BookEditor
import eu.eyan.idakonyvtar.model.Autocomplete
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.BookField
import eu.eyan.idakonyvtar.model.Multifield
import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.idakonyvtar.testhelper.AbstractUiTest
import eu.eyan.idakonyvtar.testhelper.BookEditorTestHelper
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.testutil.video.VideoRunner
import eu.eyan.util.excel.ExcelColumn
import eu.eyan.util.text.Text
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

  private var bookController: BookEditor = _

  @Before
  def setUp(): Unit = {
    val columns: List[String] =
      List("szimpla", "ac", "mm", "mmac", "cim")
    val book: Book =
      new BookBuilder(columns.size).withValue(0, "Érték1").build
    val bookList: List[Book] = List(
      book,
      new BookBuilder(columns.size)
        .withValue(0, "Érték2")
        .withValue(1, "abc")
        .withValue(3, "abc")
        .build,
      new BookBuilder(columns.size)
        .withValue(0, "Érték2")
        .withValue(1, "abd")
        .withValue(3, "abd")
        .build)

    val fields = List(
      BookField(ExcelColumn(0), "szimpla", List(), Array()),
      BookField(ExcelColumn(1), "ac", List(Autocomplete), Array()),
      BookField(ExcelColumn(2), "mm", List(Multifield), Array()),
      BookField(ExcelColumn(3), "mmac", List(Multifield, Autocomplete), Array()),
      BookField(ExcelColumn(4), "cim", List(), Array(new Marc("245", "10", "a", ""))))

    bookController = BookEditor.editWithIsbn(book, fields, bookList, null)

    SwingUtilities.invokeLater(() =>
      DialogHelper.yesNoEditor(null, bookController.getComponent, new Text("title"), new Text("save"), new Text("cancel")))
    bookEditor = new BookEditorTestHelper(
      BasicRobot.robotWithCurrentAwtHierarchy())
    VideoRunner.setComponentToRecord(bookEditor.getComponentToRecord)
  }
  /* TODO images map*/

  @After
  def tearDown(): Unit = {
    bookEditor.cleanUp()
  }

  @Test
  def testIsbnRead(): Unit = {
    bookEditor.requireIsbnPresent()
    bookEditor.setNormalText("isbnText", "9789631193701")
    bookEditor.keyboard(KeyEvent.VK_ENTER)
    bookEditor.requireNormalText("cim", "Abigél")
  }

}
