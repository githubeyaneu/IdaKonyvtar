package eu.eyan.idakonyvtar.testhelper

import java.awt.Component
import java.awt.Point
import java.io.File

import org.fest.assertions.Assertions.assertThat
import org.fest.swing.core.MouseButton
import org.fest.swing.core.matcher.JButtonMatcher
import org.fest.swing.fixture.DialogFixture
import org.fest.swing.fixture.FrameFixture

import eu.eyan.idakonyvtar.IdaLibrary
import eu.eyan.idakonyvtar.text.LanguageHandler
import eu.eyan.idakonyvtar.view.LibraryMenuAndToolBar

class IdaLibraryTestHelper {

  private var frame: FrameFixture = _

  def start(filenév: String): Unit = {
    IdaLibrary.main(Array(filenév))
    frame = new FrameFixture(classOf[IdaLibrary].getName)
    frame.target.toFront()
  }

  def requireVisible(): Unit = {
    assertThat(frame.target.isVisible).isTrue
  }

  def requireTitle(title: String): Unit = {
    assertThat(frame.target.getTitle).isEqualTo(title)
  }

  def close(): Unit = {
    frame.target.setVisible(false)
  }

  def clickMenu(menuPoint: String): Unit = {
    frame.menuItem(menuPoint).click()
  }

  def load(file: File): Unit = {
    clickMenu("File")
    clickMenu("Load")
    frame.fileChooser().selectFile(file).approve()
  }

  def assertTableCell(col: Int, row: Int, content: String): Unit = {
    assertThat(frame.table().contents()(row - 1)(col - 1)).isEqualTo(content)
  }

  def save(file: File): Unit = {
    clickMenu("File")
    clickMenu("Save")
    frame.fileChooser().selectFile(file).approve()
  }

  def clickNewButton(): Unit = {
    frame.button(LibraryMenuAndToolBar.ADD_NEW_BOOK).click()
  }

  def editor(): BookEditorTestHelper = new BookEditorTestHelper(frame.robot)

  def requireDeleteDisabled(): Unit = {
    frame.button("Book törlése").requireDisabled()
  }

  def selectRow(row: Int): Unit = {
    frame.table().selectRows(row - 1)
  }

  def requireDeleteEnabled(): Unit = {
    frame.button("Book törlése").requireEnabled()
  }

  def clickDeleteButton(): Unit = {
    frame.button("Book törlése").click()
  }

  def clickApproveYes(): Unit = {
    frame
      .dialog()
      .button(JButtonMatcher.withText(LanguageHandler.YES))
      .click()
  }

  def clickApproveNo(): Unit = {
    frame
      .dialog()
      .button(JButtonMatcher.withText(LanguageHandler.NO))
      .click()
  }

  def filter(filter: String): Unit = {
    frame.textBox(LibraryMenuAndToolBar.FILTER).robot.enterText(filter)
  }

  def assertTableRowCount(rowsCount: Int): Unit = {
    frame.table().requireRowCount(rowsCount)
  }

  def doubleClick(row: Int): Unit = {
    val rowHeight: Int = frame.table().target.getRowHeight
    val firstRow: Point = frame.table().target.getLocationOnScreen
    firstRow.translate(3, rowHeight * (row - 1) + rowHeight / 2)
    frame.table().robot.moveMouse(firstRow)
    frame.table().robot.click(firstRow, MouseButton.LEFT_BUTTON, 2)
  }

  def cleanUp(): Unit = {
    frame.cleanUp()
  }

  def checkTitleWithNumber(numberOfBooks: Int): Unit = {
    if (numberOfBooks == 0) requireTitle("IdaLibrary - no books")
    else if (numberOfBooks == 1) requireTitle("IdaLibrary - one book")
    else requireTitle("IdaLibrary - " + numberOfBooks + " books")
  }

  def clickExit(): Unit = {
    frame.close()
  }

  def exitDialog(): DialogFixture = frame.dialog()

  def requireInvisible(): Unit = {
    assertThat(frame.target.isVisible).isFalse
  }

  def getComponentToRecord(): Component = frame.target

}
