package eu.eyan.idakonyvtar.testhelper;

import static org.fest.assertions.Assertions.assertThat;

import java.awt.Component;
import java.awt.Point;
import java.io.File;

import org.fest.swing.core.MouseButton;
import org.fest.swing.core.matcher.JButtonMatcher;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;

import eu.eyan.idakonyvtar.IdaLibrary;
import eu.eyan.idakonyvtar.text.LanguageHandler;
import eu.eyan.idakonyvtar.view.LibraryMenuAndToolBar;

public class IdaLibraryTestHelper {
	private FrameFixture frame;

	public void start(String filenév) {
		IdaLibrary.main(new String[] { filenév });
		frame = new FrameFixture(IdaLibrary.class.getName());
		frame.target.toFront();
	}

	public void requireVisible() {
		assertThat(frame.target.isVisible()).isTrue();
	}

	public void requireTitle(String title) {
		assertThat(frame.target.getTitle()).isEqualTo(title);
	}

	public void close() {
		frame.target.setVisible(false);
	}

	public void clickMenu(String menuPoint) {
		frame.menuItem(menuPoint).click();
	}

	public void load(File file) {
		clickMenu("File");
		clickMenu("Load");
		frame.fileChooser().selectFile(file).approve();
	}

	public void assertTableCell(int col, int row, String content) {
		assertThat(frame.table().contents()[row - 1][col - 1]).isEqualTo(content);
	}

	public void save(File file) {
		clickMenu("File");
		clickMenu("Save");
		frame.fileChooser().selectFile(file).approve();
	}

	public void clickNewButton() {
		frame.button(LibraryMenuAndToolBar.ADD_NEW_BOOK()).click();

	}

	public BookEditorTestHelper editor() {
		return new BookEditorTestHelper(frame.robot);
	}

	public void requireDeleteDisabled() {
		frame.button("Book törlése").requireDisabled();

	}

	public void selectRow(int row) {
		frame.table().selectRows(row - 1);
	}

	public void requireDeleteEnabled() {
		frame.button("Book törlése").requireEnabled();
	}

	public void clickDeleteButton() {
		frame.button("Book törlése").click();
	}

	public void clickApproveYes() {
		frame.dialog().button(JButtonMatcher.withText(LanguageHandler.YES())).click();
	}

	public void clickApproveNo() {
		frame.dialog().button(JButtonMatcher.withText(LanguageHandler.NO())).click();
	}

	public void filter(String filter) {
		frame.textBox(LibraryMenuAndToolBar.FILTER()).robot.enterText(filter);
	}

	public void assertTableRowCount(int rowsCount) {
		frame.table().requireRowCount(rowsCount);
	}

	public void doubleClick(int row) {
		int rowHeight = frame.table().target.getRowHeight();
		Point firstRow = frame.table().target.getLocationOnScreen();
		firstRow.translate(3, rowHeight * (row - 1) + rowHeight / 2);
		frame.table().robot.moveMouse(firstRow);
		frame.table().robot.click(firstRow, MouseButton.LEFT_BUTTON, 2);
	}

	public void cleanUp() {
		frame.cleanUp();
	}

	public void checkTitleWithNumber(int numberOfBooks) {
		if (numberOfBooks == 0)
			requireTitle("IdaLibrary - no books");
		else if (numberOfBooks == 1)
			requireTitle("IdaLibrary - one book");
		else
			requireTitle("IdaLibrary - " + numberOfBooks + " books");
	}

	public void clickExit() {
		frame.close();

	}

	public DialogFixture exitDialog() {
		return frame.dialog();
	}

	public void requireInvisible() {
		assertThat(frame.target.isVisible()).isFalse();
	}

	public Component getComponentToRecord() {
		return frame.target;
	}
}