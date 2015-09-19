package eu.eyan.idakonyvtar.controller.input;

import java.util.List;

import eu.eyan.idakonyvtar.model.Book;
import eu.eyan.idakonyvtar.model.ColumnKonfiguration;

public class BookControllerInput {

	public final static boolean ISBN_ENABLED = true;

	private Book book;

	private List<String> columns;

	private boolean isbnEnabled = false;

	public boolean isIsbnEnabled() {
		return isbnEnabled;
	}

	public ColumnKonfiguration getColumnConfiguration() {
		return columnConfiguration;
	}

	public List<Book> getBookList() {
		return bookList;
	}

	private ColumnKonfiguration columnConfiguration = null;

	private List<Book> bookList;

	private BookControllerInput() {
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public static class Builder {
		private BookControllerInput input = new BookControllerInput();

		public Builder withBook(Book book) {
			this.input.setBook(book);
			return this;
		}

		public Builder withBookList(List<Book> bookList) {
			this.input.bookList = bookList;
			return this;
		}

		public Builder withColumns(List<String> columnok) {
			this.input.setColumns(columnok);
			return this;
		}

		public Builder withIsbnEnabled(boolean isbnEnabled) {
			this.input.isbnEnabled = isbnEnabled;
			return this;
		}

		public Builder withColumnConfiguration(
				ColumnKonfiguration columnConfiguration) {
			this.input.columnConfiguration = columnConfiguration;
			return this;
		}

		public BookControllerInput build() {
			if (this.input.getBook() == null) {
				throw new RuntimeException("A könyv nem lehet null");
			}
			if (this.input.bookList == null) {
				throw new RuntimeException("A könyvlista nem lehet null");
			}
			if (this.input.getColumns() == null) {
				throw new RuntimeException("Az oszlop nem lehet null");
			}
			if (this.input.columnConfiguration == null) {
				throw new RuntimeException(
						"Az oszlopkonfiguráció nem lehet null");
			}
			return this.input;
		}

	}
}
