package eu.eyan.idakonyvtar.testhelper

import org.fest.swing.finder.WindowFinder.findDialog

import java.awt.Component

import java.awt.event.KeyEvent

import javax.swing.JDialog

import javax.swing.SwingUtilities

import org.fest.assertions.Fail

import org.fest.swing.core.GenericTypeMatcher

import org.fest.swing.core.Robot

import org.fest.swing.fixture.ContainerFixture

import org.fest.swing.fixture.DialogFixture

import eu.eyan.idakonyvtar.util.DialogHelper

import eu.eyan.idakonyvtar.view.BookView

import eu.eyan.testutil.swing.fixture.AutocompleteFixture

import BookEditorTestHelper._

//remove if not needed
import scala.collection.JavaConversions._

object BookEditorTestHelper {

  val VISIBLE_DIALOG_FINDER: GenericTypeMatcher[JDialog] =
    new GenericTypeMatcher[JDialog](classOf[JDialog]) {
      protected override def isMatching(jDialog: JDialog): Boolean =
        jDialog.isVisible
    }

}

class BookEditorTestHelper(robot: Robot) {

  private var dialog: DialogFixture =
    findDialog(VISIBLE_DIALOG_FINDER).withTimeout(1000).using(robot)

  dialog.target.toFront()

  def requireIsbnNotPresent(): Unit = {
    requireLabelNotPresent(dialog, BookView.ISBN_LABEL)
    requireTextBoxNotPresent(dialog, BookView.ISBN_TEXT)
  }

  private def requireTextBoxNotPresent(container: ContainerFixture[_],
                                       textBoxName: String): Unit = {
    try container.label(textBoxName)
    catch {
      case e: Exception => return

    }
    Fail.fail()
  }

  private def requireLabelNotPresent(container: ContainerFixture[_],
                                     labelName: String): Unit = {
    try container.label(labelName).requireVisible()
    catch {
      case e: Exception => return

    }
    Fail.fail()
  }

  def requireIsbnPresent(): Unit = {
    dialog.label(BookView.ISBN_LABEL).requireVisible()
    dialog.textBox(BookView.ISBN_TEXT).requireVisible()
  }

  def setNormalText(textBoxNév: String, szöveg: String): Unit = {
    dialog.textBox(textBoxNév).setText(szöveg)
  }

  def clickSave(): Unit = {
    dialog.button("Mentés").click()
  }

  def clickCancel(): Unit = {
    dialog.button("Mégsem").click()
  }

  def cleanUp(): Unit = {
    dialog.cleanUp()
  }

  def setComboBoxText(comboBoxName: String, value: String): Unit = {
    dialog.comboBox(comboBoxName).enterText(value)
  }

  def keyboard(keyCode: Int): Unit = {
    dialog.robot.pressKey(keyCode)
  }

  def enterNormalText(textBoxName: String, text: String): Unit = {
    dialog.textBox(textBoxName).click()
    dialog.textBox(textBoxName).robot.enterText(text)
  }

  def multifieldDelete(columnName: String, counter: Int): Unit = {
    dialog.button(columnName + ".delete." + counter).click()
  }

  def requireDeleteDisabled(columnName: String, counter: Int): Unit = {
    dialog.button(columnName + ".delete." + counter).requireDisabled()
  }

  def enterComboBoxText(comboBoxName: String, text: String): Unit = {
    dialog.comboBox(comboBoxName).click()
    dialog.comboBox(comboBoxName).robot.enterText(text)
    dialog.comboBox(comboBoxName).robot.pressKey(KeyEvent.VK_ESCAPE)
  }

  def requireNormalText(textBoxName: String, text: String): Unit = {
    dialog.textBox(textBoxName).requireText(text)
  }

  def getComponentToRecord(): Component = SwingUtilities.getRoot(dialog.target)

  def autocomplete(name: String): AutocompleteFixture =
    new AutocompleteFixture(dialog, name)

}
