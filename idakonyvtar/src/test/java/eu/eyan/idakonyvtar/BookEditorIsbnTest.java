package eu.eyan.idakonyvtar;

import static com.google.common.collect.Lists.newArrayList;

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
import eu.eyan.idakonyvtar.view.BookView;
import eu.eyan.testutil.video.VideoRunner;

public class BookEditorIsbnTest extends AbstractUiTest {
	private BookEditorTestHelper bookEditor;
	private BookController bookController;

	@Before
	public void setUp() {
		List<String> columns = newArrayList("szimpla", "ac", "mm", "mmac", "cim");
		Book book = new Book.Builder(columns.size()).withValue(0, "Érték1").build();

		ColumnKonfiguration columnConfig = new ColumnKonfiguration.Builder(4, columns.size() + 1)
				.withRow("", ColumnConfigurations.MULTIFIELD().name(), ColumnConfigurations.AUTOCOMPLETE().name(), ColumnConfigurations.MARC_CODE().name())
				.withRow(columns.get(0), "", "", "").withRow(columns.get(1), "", "igen", "").withRow(columns.get(2), "igen", "", "")
				.withRow(columns.get(3), "igen", "igen", "").withRow(columns.get(4), "nem", "nem", "245-10-a").build();

		ArrayList<Book> bookList = newArrayList(book, new Book.Builder(columns.size()).withValue(0, "Érték2").withValue(1, "abc").withValue(3, "abc").build(),
				new Book.Builder(columns.size()).withValue(0, "Érték2").withValue(1, "abd").withValue(3, "abd").build());

		bookController = new BookController();
		BookControllerInput bookControllerInput = new BookControllerInput(book, JavaConversions.asScalaBuffer(columns).toList(), columnConfig, JavaConversions
				.asScalaBuffer(bookList).toList(), true, null/* FIXME images map*/ );

		SwingUtilities.invokeLater(() -> DialogHelper.startModalDialog(null, bookController, bookControllerInput));

		bookEditor = new BookEditorTestHelper(BasicRobot.robotWithCurrentAwtHierarchy());

		VideoRunner.setComponentToRecord(bookEditor.getComponentToRecord());
	}

	@After
	public void tearDown() {
		bookEditor.cleanUp();
	}

	@Test
	public void testIsbnRead() {
		bookEditor.requireIsbnPresent();
		bookEditor.setNormalText(BookView.ISBN_TEXT(), "9789631193701");
		bookEditor.keyboard(KeyEvent.VK_ENTER);
		bookEditor.requireNormalText("cim", "Abigél");
	}

	public static void main(String[] args) {
		BookEditorIsbnTest test = new BookEditorIsbnTest();
		test.setUp();
		test.testIsbnRead();
	}
}