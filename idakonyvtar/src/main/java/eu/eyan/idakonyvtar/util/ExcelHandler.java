package eu.eyan.idakonyvtar.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import eu.eyan.idakonyvtar.model.Book;
import eu.eyan.idakonyvtar.model.LibraryS;

public class ExcelHandler {
	public static final String COLUMN_CONFIGURATION = "OszlopKonfiguráció";
	public static final String BOOKS = "Könyvek";

	public static LibraryS readLibrary(final File file) {
		backup(file);
		LibraryS library = new LibraryS();
		try {
			Workbook workbook = Workbook.getWorkbook(file,
					getWorkbookSettings());
			readBooks(library, getSheet(getWorkbookSettings(), workbook, BOOKS));
			readColumnConfiguration(
					library,
					getSheet(getWorkbookSettings(), workbook,
							COLUMN_CONFIGURATION));
		} catch (BiffException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Biff Hiba a beolvasásnál " + e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Hiba a beolvasásnál " + e.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
		}
		return library;
	}

	public static WorkbookSettings getWorkbookSettings() {
		WorkbookSettings ws = new WorkbookSettings();
		ws.setEncoding("Cp1252");
		return ws;
	}

	private static Sheet getSheet(WorkbookSettings ws, Workbook workbook,
			String string) {
		Sheet sheet = workbook.getSheet(new String(string.getBytes(Charset
				.forName(ws.getEncoding()))));
		if (sheet == null) {
			sheet = workbook.getSheet(string);
		}
		return sheet;
	}

	private static void readColumnConfiguration(LibraryS library, Sheet sheet) {
		String[][] table = new String[sheet.getColumns()][sheet.getRows()];
		for (int actualColumn = 0; actualColumn < sheet.getColumns(); actualColumn++) {
			for (int actualRow = 0; actualRow < sheet.getRows(); actualRow++) {
				table[actualColumn][actualRow] = sheet.getCell(actualColumn,
						actualRow).getContents();
			}
		}
		library.getConfiguration().setTable(table);
	}

	private static void readBooks(LibraryS library, Sheet sheet) {
		for (int actualColumn = 0; actualColumn < sheet.getColumns(); actualColumn++) {
			library.getColumns().add(
					sheet.getCell(actualColumn, 0).getContents());
		}

		for (int actualRow = 1; actualRow < sheet.getRows(); actualRow++) {
			Book book = new Book(sheet.getColumns() + 1);
			// boolean isEmpty = true;
			for (int actualColumn = 0; actualColumn < sheet.getColumns(); actualColumn++) {
				String contents = sheet.getCell(actualColumn, actualRow)
						.getContents();
				// if (contents != null && contents.length() != 0)
				// {
				// isEmpty = false;
				// System.out.println("-" + contents + "-");
				// }
				book.setValue(actualColumn, contents);
			}
			library.getBooks().add(book);
		}
	}

	public static void saveLibrary(File targetFile, LibraryS library) {
		if (targetFile.exists() && !targetFile.isFile()) {
			System.out.println("not File");
			return;
		}

		if (targetFile.exists()) {
			backup(targetFile);
			try {
				FileUtils.forceDelete(targetFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			WritableWorkbook workbook = Workbook.createWorkbook(targetFile,
					getWorkbookSettings());
			WritableSheet writablesheet1 = workbook.createSheet(new String(
					BOOKS), 0);
			for (int columnIndex = 0; columnIndex < library.getColumns().size(); columnIndex++) {
				writablesheet1.addCell(new Label(columnIndex, 0, library
						.getColumns().get(columnIndex)));
				for (int bookIndex = 0; bookIndex < library.getBooks().size(); bookIndex++) {
					WritableCellFormat cellFormat = new WritableCellFormat();
					cellFormat.setWrap(true);
					writablesheet1.addCell(new Label(columnIndex,
							bookIndex + 1, library.getBooks().get(bookIndex)
									.getValue(columnIndex), cellFormat));
				}
			}
			WritableSheet writablesheet2 = workbook.createSheet(
					COLUMN_CONFIGURATION, 1);
			String[][] table = library.getConfiguration().getTable();
			for (int column = 0; column < table.length; column++) {
				for (int sor = 0; sor < table[0].length; sor++) {
					writablesheet2.addCell(new Label(column, sor,
							table[column][sor]));
				}
			}
			// FIXME: save konfiguration... question: is it possible to change
			// any konfiguration from the application?
			workbook.write();
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	private static void backup(File fileToSave) {
		String sourceLibrary = FilenameUtils.getFullPath(fileToSave
				.getAbsolutePath());
		String sourceFileName = FilenameUtils.getName(fileToSave
				.getAbsolutePath());
		File backupLibrary = new File(sourceLibrary + "backup");
		backupLibrary.mkdirs();
		File backupFile = new File(
				backupLibrary.getAbsoluteFile()
						+ File.separator
						+ sourceFileName
						+ "_backup_"
						+ new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
								.format(new Date()) + ".zip");
		BackupHelper.zipFile(fileToSave, backupFile);
	}
}
