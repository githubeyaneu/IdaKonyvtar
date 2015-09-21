package eu.eyan.idakonyvtar.testhelper;

import static eu.eyan.idakonyvtar.util.ExcelHandler.getWorkbookSettings;
import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ExcelAssert {

	public static void assertExcelCell(File sourceFile, String sheetName,
			int column, int row, String expected) {
		Workbook workbook = null;
		try {
			workbook = Workbook.getWorkbook(sourceFile, getWorkbookSettings());
			assertThat(
					workbook.getSheet(sheetName).getCell(column - 1, row - 1)
							.getContents()).isEqualTo(expected);
		} catch (BiffException | IOException e) {
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}

	}

}
