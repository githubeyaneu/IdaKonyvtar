package eu.eyan.idakonyvtar.model;

import com.jgoodies.binding.list.SelectionInList;

public class LibraryModel {
	private SelectionInList<Book> books = new SelectionInList<Book>();

	private Library library;

	public SelectionInList<Book> getBooks() {
		return books;
	}

	public Library getLibrary() {
		return library;
	}

	public void setLibrary(Library library) {
		this.library = library;
	}

}