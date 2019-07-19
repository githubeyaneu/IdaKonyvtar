package eu.eyan.idakonyvtar.testhelper

import java.awt.Component
import java.awt.event.KeyEvent

import eu.eyan.idakonyvtar.testhelper.BookEditorTestHelper._
import eu.eyan.testutil.swing.fixture.AutocompleteFixture
import javax.swing.{JDialog, SwingUtilities}
import org.fest.assertions.Fail
import org.fest.swing.core.{GenericTypeMatcher, Robot}
import org.fest.swing.finder.WindowFinder.findDialog
import org.fest.swing.fixture.{ContainerFixture, DialogFixture}

//remove if not needed

object BookEditorTestHelper {

  val VISIBLE_DIALOG_FINDER: GenericTypeMatcher[JDialog] =
    new GenericTypeMatcher[JDialog](classOf[JDialog]) {
      protected override def isMatching(jDialog: JDialog): Boolean =
        jDialog.isVisible
    }

}

class BookEditorTestHelper(robot: Robot) {

  private val dialog: DialogFixture =
    findDialog(VISIBLE_DIALOG_FINDER).withTimeout(1000).using(robot)

  dialog.target.toFront()

  def requireIsbnNotPresent(): Unit = {
    requireLabelNotPresent(dialog, "isbnLabel")
    requireTextBoxNotPresent(dialog, "isbnText")
  }

  private def requireTextBoxNotPresent(container: ContainerFixture[_],
                                       textBoxName: String): Unit = {
    try container.label(textBoxName)
    catch {
      case _: Exception => return
    }
    Fail.fail()
  }

  private def requireLabelNotPresent(container: ContainerFixture[_],
                                     labelName: String): Unit = {
    try container.label(labelName).requireVisible()
    catch {
      case _: Exception => return
    }
    Fail.fail()
  }

  def requireIsbnPresent(): Unit = {
    dialog.label("isbnLabel").requireVisible()
    dialog.textBox("isbnText").requireVisible()
  }

  def setNormalText(textBoxNév: String, szöveg: String): Unit = {
    dialog.textBox(textBoxNév).setText(szöveg)
  }

  def clickSave(): Unit = {
    dialog.button("Save").click()
  }

  def clickCancel(): Unit = {
    dialog.button("Cancel").click()
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

  def getComponentToRecord: Component = SwingUtilities.getRoot(dialog.target)

  def autocomplete(name: String): AutocompleteFixture =
    new AutocompleteFixture(dialog, name)

}
