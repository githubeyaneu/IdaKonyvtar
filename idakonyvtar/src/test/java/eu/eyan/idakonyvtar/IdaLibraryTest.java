package eu.eyan.idakonyvtar;

import java.io.File;

import org.fest.swing.core.matcher.JButtonMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.eyan.idakonyvtar.model.ColumnConfigurations;
import eu.eyan.idakonyvtar.testhelper.IdaLibraryTestHelper;
import eu.eyan.idakonyvtar.testhelper.LibraryFileBuilder;
import eu.eyan.idakonyvtar.util.ExcelHandler;
import eu.eyan.idakonyvtar.view.LibraryMenuAndToolBar;
import eu.eyan.testutil.ExcelAssert;
import eu.eyan.testutil.video.VideoRunner;
import eu.eyan.util.swing.HighlightRenderer;

public class IdaLibraryTest extends AbstractUiTest {
	private static IdaLibraryTestHelper library = new IdaLibraryTestHelper();

	@Before
	public void setUp() {
		library.start(null);
		VideoRunner.setComponentToRecord(library.getComponentToRecord());
	}

	@After
	public void tearDown() {
		library.cleanUp();
	}

	@Test
	public void testStartProgram() {
		library.requireVisible();
		library.checkTitleWithNumber(4);
	}

	@Test
	@Ignore
	// FIXME: Implement next feature
	public void testMenu() {
		library.clickMenu(LibraryMenuAndToolBar.ISBN_SEARCH());
		library.editor().clickCancel();
		library.clickMenu(LibraryMenuAndToolBar.FILE());
	}

	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "test cleanup")
	public void testLoadAndSave() {
		File file = new LibraryFileBuilder().withSheet(ExcelHandler.BOOKS()).withColumns("column1", "column2")
				.withRow("árvíztűrő tükörfúrógép", "ÁRVÍZTŰRŐ TÜKÖRFÚRÓGÉP").withSheet(ExcelHandler.COLUMN_CONFIGURATION())
				.withColumns("", ColumnConfigurations.SHOW_IN_TABLE().name(), "ko2").withRow("column1", "igen", "").withRow("column2 tükörfúrógép", "nem", "")
				.save();
		try {
			library.load(file);
			library.assertTableCell(1, 1, "árvíztűrő tükörfúrógép");
			library.checkTitleWithNumber(1);
		} finally {
			file.delete();
		}
		File file2 = new File(System.currentTimeMillis() + ".xls");
		try {
			library.save(file2);
			ExcelAssert.assertExcelCell(file2, ExcelHandler.BOOKS(), 1, 2, "árvíztűrő tükörfúrógép");
			library.checkTitleWithNumber(1);
		} finally {
			file2.delete();
		}
	}

	@Test
	public void testSaveNewBook() throws Exception {
		library.assertTableCell(2, 1, "original title 1");
		library.clickNewButton();
		library.editor().requireIsbnPresent();
		library.editor().setNormalText("Cím", "New Title 1");
		library.editor().clickSave();
		library.assertTableCell(2, 1, "New Title 1");
		library.assertTableCell(2, 2, "original title 1");
		library.checkTitleWithNumber(5);
	}

	@Test
	public void testNewBookSaveNot() {
		library.assertTableCell(2, 1, "original title 1");
		library.clickNewButton();
		library.editor().setNormalText("Cím", "New Title 1");
		library.editor().clickCancel();
		library.assertTableCell(2, 1, "original title 1");
		library.checkTitleWithNumber(4);
	}

	@Test
	public void testBookDeleteOk() {
		library.assertTableCell(2, 1, "original title 1");
		library.requireDeleteDisabled();
		library.selectRow(1);
		library.requireDeleteEnabled();
		library.clickDeleteButton();
		library.clickApproveYes();
		library.assertTableCell(2, 1, "original title 2");
		library.requireDeleteDisabled();
		library.checkTitleWithNumber(3);
	}

	@Test
	public void testBookDeleteCancel() {
		library.assertTableCell(2, 1, "original title 1");
		library.requireDeleteDisabled();
		library.selectRow(1);
		library.requireDeleteEnabled();
		library.clickDeleteButton();
		library.clickApproveNo();
		library.assertTableCell(2, 1, "original title 1");
		library.requireDeleteEnabled();
		library.checkTitleWithNumber(4);
	}

	@Test
	public void testFilter() {
		library.filter("aron");
		library.assertTableCell(
				1,
				1,
				new StringBuilder("").append(HighlightRenderer.HTML_START_TAG()).append("Tamási ").append(HighlightRenderer.HIGHLIGHT_START_TAG())
						.append("Áron").append(HighlightRenderer.HIGHLIGHT_END_TAG()).append(HighlightRenderer.HTML_END_TAG()).toString());
		library.assertTableCell(2, 2,
				new StringBuilder("").append(HighlightRenderer.HTML_START_TAG()).append("Kh").append(HighlightRenderer.HIGHLIGHT_START_TAG()).append("áron")
						.append(HighlightRenderer.HIGHLIGHT_END_TAG()).append(" ladikján").append(HighlightRenderer.HTML_END_TAG()).toString());
		library.assertTableCell(1, 2, "Illyés Gyula");
		library.assertTableRowCount(2);
	}

	@Test
	public void testBookEditSave() {
		library.assertTableCell(2, 1, "original title 1");
		library.doubleClick(1);
		library.editor().requireIsbnNotPresent();
		library.editor().setNormalText("Cím", "New Title 1");
		library.editor().clickSave();
		library.assertTableCell(2, 1, "New Title 1");
		library.checkTitleWithNumber(4);
	}

	@Test
	public void testBookEditCancel() {
		library.assertTableCell(2, 1, "original title 1");
		library.doubleClick(1);
		library.editor().setNormalText("Cím", "New Title");
		library.editor().clickCancel();
		library.assertTableCell(2, 1, "original title 1");
		library.checkTitleWithNumber(4);
	}

	@Test
	public void testExitNo() {
		library.requireVisible();
		library.clickExit();
		library.exitDialog().requireVisible();
		library.exitDialog().button(JButtonMatcher.withText("Mégsem")).click();
		library.requireVisible();
	}

	@Test
	public void testExitYes() {
		library.requireVisible();
		library.clickExit();
		library.exitDialog().requireVisible();
		library.exitDialog().button(JButtonMatcher.withText("Igen")).click();
		library.requireInvisible();
	}
}
