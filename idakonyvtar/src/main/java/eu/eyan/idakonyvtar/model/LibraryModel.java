package eu.eyan.idakonyvtar.model;

import lombok.Getter;
import lombok.Setter;

import com.jgoodies.binding.list.SelectionInList;

public class LibraryModel
{
    @Getter
    private SelectionInList<Book> books = new SelectionInList<Book>();

    @Getter
    @Setter
    private Library library;
}