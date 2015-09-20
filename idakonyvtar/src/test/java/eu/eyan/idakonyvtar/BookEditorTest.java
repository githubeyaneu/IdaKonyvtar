package eu.eyan.idakonyvtar;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eyan.idakonyvtar.model.ColumnKonfiguration.ColumnConfigurations.AUTOCOMPLETE;
import static eu.eyan.idakonyvtar.model.ColumnKonfiguration.ColumnConfigurations.MULTIFIELD;
import static org.fest.assertions.Assertions.assertThat;

import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.SwingUtilities;

import org.fest.swing.core.BasicRobot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eyan.idakonyvtar.controller.BookController;
import eu.eyan.idakonyvtar.controller.input.BookControllerInput;
import eu.eyan.idakonyvtar.model.Book;
import eu.eyan.idakonyvtar.model.ColumnKonfiguration;
import eu.eyan.idakonyvtar.testhelper.BookEditorTestHelper;
import eu.eyan.idakonyvtar.util.DialogHelper;

public class BookEditorTest extends AbstractUiTest {
	private BookEditorTestHelper bookEditor;
	private BookController bookController;

	@Before
	public void setUp() {
		List<String> columns = newArrayList("szimpla", "ac", "mm", "mmac");
		Book book = new Book.Builder(columns.size()).withValue(0, "Érték1")
				.build();
		bookController = new BookController();
		BookControllerInput bookControllerInput = new BookControllerInput(book,
				columns, new ColumnKonfiguration.Builder(3, columns.size() + 1)
						.withRow("", MULTIFIELD.getName(),
								AUTOCOMPLETE.getName())
						.withRow(columns.get(0), "", "")
						.withRow(columns.get(1), "", "igen")
						.withRow(columns.get(2), "igen", "")
						.withRow(columns.get(3), "igen", "igen").build(),
				newArrayList(book,
						new Book.Builder(columns.size()).withValue(0, "Érték2")
								.withValue(1, "abc").withValue(3, "abc")
								.build(), new Book.Builder(columns.size())
								.withValue(0, "Érték2").withValue(1, "abd")
								.withValue(3, "abd").build()), false);
		SwingUtilities.invokeLater(() -> DialogHelper.startModalDialog(null,
				bookController, bookControllerInput));
		bookEditor = new BookEditorTestHelper(
				BasicRobot.robotWithCurrentAwtHierarchy());
	}

	@After
	public void tearDown() {
		bookEditor.cleanUp();
	}

	@Test
	public void testNormalField() {
		bookEditor.setNormalText("szimpla", "szimpla");
		bookEditor.clickSave();
		assertThat(bookController.getOutput().getValue(0)).isEqualTo("szimpla");
	}

	@Test
	public void testAutocompleteDefault() {
		bookEditor.setComboBoxText("ac", "a");
		bookEditor.keyboard(KeyEvent.VK_ESCAPE);
		bookEditor.clickSave();
		assertThat(bookController.getOutput().getValue(1)).isEqualTo("abc");
	}

	@Test
	public void testAutocompleteNew() {
		bookEditor.setComboBoxText("ac", "a");
		bookEditor.keyboard(KeyEvent.VK_DELETE);
		bookEditor.keyboard(KeyEvent.VK_ESCAPE);
		bookEditor.clickSave();
		assertThat(bookController.getOutput().getValue(1)).isEqualTo("a");
	}

	@Test
	public void testMultiField() {
		bookEditor.setNormalText("mm1", "");
		bookEditor.enterNormalText("mm1", "a");
		bookEditor.enterNormalText("mm2", "b");
		bookEditor.enterNormalText("mm1", "b");
		bookEditor.enterNormalText("mm3", "c");
		bookEditor.multifieldDelete("mm", 2);
		bookEditor.requireDeleteDisabled("mm", 4);
		bookEditor.clickSave();
		assertThat(bookController.getOutput().getValue(2)).isEqualTo("ab + c");
	}

	@Test
	public void testMultiFieldAutoComplete() {
		bookEditor.setComboBoxText("mmac1", "");
		bookEditor.enterComboBoxText("mmac1", "a");
		bookEditor.enterComboBoxText("mmac2", "b");
		bookEditor.enterComboBoxText("mmac1", "b");
		bookEditor.enterComboBoxText("mmac3", "c");
		bookEditor.multifieldDelete("mmac", 2);
		bookEditor.requireDeleteDisabled("mmac", 4);
		bookEditor.enterComboBoxText("mmac4", "a");
		bookEditor.clickSave();
		assertThat(bookController.getOutput().getValue(3)).isEqualTo(
				"b + c + abc");
	}

	@Test
	public void test2() {
		bookEditor.clickSave();
	}

	public static void main(String[] args) {
		new BookEditorTest().setUp();
	}
}
