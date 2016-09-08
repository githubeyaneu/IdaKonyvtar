package eu.eyan.idakonyvtar.testhelper;

import static org.fest.swing.finder.WindowFinder.findDialog;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.fest.assertions.Fail;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.ContainerFixture;
import org.fest.swing.fixture.DialogFixture;

import eu.eyan.idakonyvtar.util.DialogHelper;
import eu.eyan.idakonyvtar.view.BookView;
import eu.eyan.testutil.swing.fixture.AutocompleteFixture;

public class BookEditorTestHelper {

	public static final GenericTypeMatcher<JDialog> VISIBLE_DIALOG_FINDER = new GenericTypeMatcher<JDialog>(JDialog.class) {
		@Override
		protected boolean isMatching(JDialog jDialog) {
			return jDialog.isVisible();
		}
	};

	private DialogFixture dialog;

	public BookEditorTestHelper(Robot robot) {
		dialog = findDialog(VISIBLE_DIALOG_FINDER).withTimeout(1000).using(robot);
		dialog.target.toFront();
	}

	public void requireIsbnNotPresent() {
		requireLabelNotPresent(dialog, BookView.ISBN_LABEL());
		requireTextBoxNotPresent(dialog, BookView.ISBN_TEXT());
	}

	private void requireTextBoxNotPresent(ContainerFixture<?> container, String textBoxName) {
		try {
			container.label(textBoxName);
		} catch (Exception e) {
			return;
		}
		Fail.fail();
	}

	private void requireLabelNotPresent(ContainerFixture<?> container, String labelName) {
		try {
			container.label(labelName).requireVisible();
		} catch (Exception e) {
			return;
		}
		Fail.fail();
	}

	public void requireIsbnPresent() {
		dialog.label(BookView.ISBN_LABEL()).requireVisible();
		dialog.textBox(BookView.ISBN_TEXT()).requireVisible();
	}

	public void setNormalText(String textBoxNév, String szöveg) {
		dialog.textBox(textBoxNév).setText(szöveg);
	}

	public void clickSave() {
		dialog.button(DialogHelper.SAVE()).click();
	}

	public void clickCancel() {
		dialog.button(DialogHelper.CANCEL()).click();
	}

	public void cleanUp() {
		dialog.cleanUp();
	}

	public void setComboBoxText(String comboBoxName, String value) {
		dialog.comboBox(comboBoxName).enterText(value);
	}

	public void keyboard(int keyCode) {
		dialog.robot.pressKey(keyCode);
	}

	public void enterNormalText(String textBoxName, String text) {
		dialog.textBox(textBoxName).click();
		dialog.textBox(textBoxName).robot.enterText(text);
	}

	public void multifieldDelete(String columnName, int counter) {
		dialog.button(columnName + ".delete." + counter).click();
	}

	public void requireDeleteDisabled(String columnName, int counter) {
		dialog.button(columnName + ".delete." + counter).requireDisabled();

	}

	public void enterComboBoxText(String comboBoxName, String text) {
		dialog.comboBox(comboBoxName).click();
		dialog.comboBox(comboBoxName).robot.enterText(text);
		dialog.comboBox(comboBoxName).robot.pressKey(KeyEvent.VK_ESCAPE);
	}

	public void requireNormalText(String textBoxName, String text) {
		dialog.textBox(textBoxName).requireText(text);
	}

	public Component getComponentToRecord() {
		return SwingUtilities.getRoot(dialog.target);
	}

	public AutocompleteFixture autocomplete(String name) {
		return new AutocompleteFixture(dialog, name);
	}
}