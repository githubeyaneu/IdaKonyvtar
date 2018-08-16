package eu.eyan.idakonyvtar;

import static com.google.common.collect.Lists.newArrayList;
import static org.fest.assertions.Assertions.assertThat;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.fest.swing.core.BasicRobot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import scala.collection.JavaConversions;
import eu.eyan.idakonyvtar.controller.BookController;
import eu.eyan.idakonyvtar.controller.input.BookControllerInput;
import eu.eyan.idakonyvtar.model.Book;
import eu.eyan.idakonyvtar.model.ColumnConfigurations;
import eu.eyan.idakonyvtar.model.ColumnKonfiguration;
import eu.eyan.idakonyvtar.testhelper.BookEditorTestHelper;
import eu.eyan.idakonyvtar.util.DialogHelper;
import eu.eyan.log.Log;
import eu.eyan.testutil.video.VideoRunner;

public class BookEditorTest extends AbstractUiTest {
	private BookEditorTestHelper bookEditor;
	private BookController bookController;

	@Before
	public void setUp() {
		Log.activateInfoLevel();

		List<String> columns = newArrayList("szimpla", "ac", "mm", "mmac");
		Book book = new Book.Builder(columns.size()).withValue(0, "Érték1").build();

		ColumnKonfiguration columnConfiguration = new ColumnKonfiguration.Builder(3, columns.size() + 1)
				.withRow("", ColumnConfigurations.MULTIFIELD().name(), ColumnConfigurations.AUTOCOMPLETE().name()).withRow(columns.get(0), "", "")
				.withRow(columns.get(1), "", "igen").withRow(columns.get(2), "igen", "").withRow(columns.get(3), "igen", "igen").build();

		ArrayList<Book> bookList = newArrayList(book, new Book.Builder(columns.size()).withValue(0, "Érték2").withValue(1, "abc").withValue(3, "abc").build(),
				new Book.Builder(columns.size()).withValue(0, "Érték2").withValue(1, "abd").withValue(3, "abd").build());

		bookController = new BookController();
		BookControllerInput bookControllerInput = new BookControllerInput(book, JavaConversions.asScalaBuffer(columns).toList(), columnConfiguration,
				JavaConversions.asScalaBuffer(bookList).toList(), false, null/* FIXME images map*/ );

		SwingUtilities.invokeLater(() -> DialogHelper.startModalDialog(null, bookController, bookControllerInput));

		bookEditor = new BookEditorTestHelper(BasicRobot.robotWithCurrentAwtHierarchy());

		VideoRunner.setComponentToRecord(bookEditor.getComponentToRecord());

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
		bookEditor.setNormalText("ac", "a");
		bookEditor.keyboard(KeyEvent.VK_ESCAPE);
		bookEditor.clickSave();
		assertThat(bookController.getOutput().getValue(1)).isEqualTo("a");
	}

	@Test
	public void testAutocompleteNew() {
		bookEditor.enterNormalText("ac", "a");
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
		bookEditor.setNormalText("mmac1", "");
		bookEditor.enterNormalText("mmac1", "a");
		bookEditor.autocomplete("mmac1").pressEscape();
		bookEditor.enterNormalText("mmac2", "b");
		bookEditor.autocomplete("mmac2").pressEscape();
		bookEditor.enterNormalText("mmac1", "b");
		bookEditor.autocomplete("mmac1").pressEscape();
		bookEditor.enterNormalText("mmac3", "c");
		bookEditor.autocomplete("mmac3").pressEscape();
		bookEditor.multifieldDelete("mmac", 2);
		bookEditor.requireDeleteDisabled("mmac", 4);
		bookEditor.enterNormalText("mmac4", "a");
		bookEditor.autocomplete("mmac4").pressEscape();
		bookEditor.clickSave();
		assertThat(bookController.getOutput().getValue(3)).isEqualTo("ab + c + a");
	}

	@Test
	public void test2() {
		bookEditor.clickSave();
	}

	public static void main(String[] args) {
		new BookEditorTest().setUp();
	}
}
