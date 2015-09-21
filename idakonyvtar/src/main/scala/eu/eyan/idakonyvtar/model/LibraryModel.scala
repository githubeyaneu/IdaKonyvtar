package eu.eyan.idakonyvtar.model;

import com.jgoodies.binding.list.SelectionInList;

class LibraryModel() {
  val books = new SelectionInList[Book]();

  var library: Library = null

  def getBooks(): SelectionInList[Book] = books

  def getLibrary() = library

  def setLibrary(library: Library) = this.library = library;
}