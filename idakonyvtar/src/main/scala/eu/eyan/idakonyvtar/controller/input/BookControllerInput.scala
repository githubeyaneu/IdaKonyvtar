package eu.eyan.idakonyvtar.controller.input;

import eu.eyan.idakonyvtar.model.Book;
import eu.eyan.idakonyvtar.model.ColumnKonfiguration;

class BookControllerInput(book: Book, columns: java.util.List[String], columnConfiguration: ColumnKonfiguration, bookList: java.util.List[Book], isbnEnabled: Boolean = false) {
  def isIsbnEnabled() = isbnEnabled
  def getColumnConfiguration() = columnConfiguration
  def getBookList() = bookList
  def getBook(): Book = book
  def getColumns() = columns
}