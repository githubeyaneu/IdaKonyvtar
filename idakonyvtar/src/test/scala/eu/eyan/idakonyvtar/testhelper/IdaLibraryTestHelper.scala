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
import eu.eyan.testutil.TestPlus
import org.fest.swing.fixture.JMenuItemFixture
import eu.eyan.log.Log
import org.fest.swing.core.Robot
import org.fest.swing.core.BasicRobot
import org.fest.swing.exception.ComponentLookupException

class IdaLibraryTestHelper extends TestPlus {

	lazy val robot = BasicRobot.robotWithCurrentAwtHierarchy
  lazy val frame = new FrameFixture(robot, classOf[IdaLibrary].getName)
  def dialog = frame.dialog
  
  def start: IdaLibraryTestHelper = start()
  def start(filenév: String = null) = { IdaLibrary.main(Array(filenév)); this }


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

  def save(file: File): Unit = {
    clickMenu("File")
    clickMenu("Save")
    frame.fileChooser().selectFile(file).approve()
  }

  def clickNewButton = frame.button(LibraryMenuAndToolBar.ADD_NEW_BOOK).click()

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

  def cleanUp = frame.cleanUp

  def checkTitleWithNumber(numberOfBooks: Int): Unit = {
    if (numberOfBooks == 0) requireTitle("IdaLibrary - no books")
    else if (numberOfBooks == 1) requireTitle("IdaLibrary - one book")
    else requireTitle("IdaLibrary - " + numberOfBooks + " books")
  }

  def clickExit = frame.close()

  def requireInvisible = assertThat(frame.target.isVisible).isFalse
  
  def requireNotExists = expectThrowable(classOf[ComponentLookupException], {frame})

  def getComponentToRecord = frame.target
}
