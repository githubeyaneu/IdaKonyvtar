package eu.eyan.idakonyvtar.testhelper

import java.awt.{Dialog, Point}
import java.io.File

import eu.eyan.idakonyvtar.IdaLibrary
import eu.eyan.testutil.TestPlus
import org.fest.assertions.Assertions.assertThat
import org.fest.swing.core._
import org.fest.swing.core.matcher.JButtonMatcher
import org.fest.swing.exception.ComponentLookupException
import org.fest.swing.fixture.{DialogFixture, FrameFixture, JMenuItemFixture}
import org.fest.swing.timing.{Pause, Timeout}

class IdaLibraryTestHelper extends TestPlus {

  var robot: Robot = _

  def dialog = {
    if (robot == null) robot = BasicRobot.robotWithCurrentAwtHierarchy
    val matcher = new TypeMatcher(classOf[Dialog], true)
    val description = "dialog to be found using matcher " + matcher
    val condition = new ComponentFoundCondition(description, robot.finder(), matcher)
    Pause.pause(condition, Timeout.timeout(100))
    new DialogFixture(robot, condition.found.asInstanceOf[Dialog])
  }

  def frame = {
    if (robot == null) robot = BasicRobot.robotWithCurrentAwtHierarchy
    new FrameFixture(robot, classOf[IdaLibrary].getName)
  }

  def cleanUp =
    if (robot != null) {
      robot.cleanUp
      pause(1000)
    }

  def start = { IdaLibrary.main(Array("library.xls")); this }
  def start(filenév: String) = { IdaLibrary.main(Array(filenév)); this }

  //noinspection AccessorLikeMethodIsUnit
  def toFront = frame.target.toFront

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

  implicit class JMenuItemFixtureImplicit(menuItem: JMenuItemFixture) {
    def requireText(expected: String) = menuItem.component.getText ==> expected
  }
  def requireMenuText(expected: String, path: String*) = menuItem(path: _*).requireText(expected)

  def menuItem(path: String*) = frame.menuItemWithPath(path: _*)

  def load(file: File): Unit = {
    clickMenu("File")
    clickMenu("Load")
    frame.fileChooser().selectFile(file).approve()
  }

  def assertTableCell(col: Int, row: Int, content: String) = assertThat(frame.table().contents()(row - 1)(col - 1)).isEqualTo(content)

  def save: Unit = {
    clickMenu("File")
    clickMenu("Save")
  }

  def saveAs(file: File): Unit = {
		  clickMenu("File")
		  clickMenu("Save as")
		  frame.fileChooser().selectFile(file).approve()
  }

  def clickNewButton = frame.button("Új book hozzáadása").click()

  def editor = new BookEditorTestHelper(frame.robot)

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
      .button(JButtonMatcher.withText("Yes"))
      .click()
  }

  def clickApproveNo(): Unit = {
    frame
      .dialog()
      .button(JButtonMatcher.withText("Cancel"))
      .click()
  }

  def filter(filter: String): Unit = {
    frame.textBox("filter").robot.enterText(filter)
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

  def checkTitleWithNumber(numberOfBooks: Int): Unit = {
    if (numberOfBooks == 0) requireTitle("IdaLibrary - no books")
    else if (numberOfBooks == 1) requireTitle("IdaLibrary - one book")
    else requireTitle("IdaLibrary - " + numberOfBooks + " books")
  }

  def clickExit = frame.close()

  def requireInvisible = assertThat(frame.target.isVisible).isFalse

  def requireNotExists = expectThrowable(classOf[ComponentLookupException], frame)

  def getComponentToRecord = frame.target
}
