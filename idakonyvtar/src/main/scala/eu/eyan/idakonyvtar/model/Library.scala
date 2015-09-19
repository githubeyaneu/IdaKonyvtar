package eu.eyan.idakonyvtar.model

import java.util.ArrayList

class Library {

  var books: java.util.List[Book] = new ArrayList()

  var columns: java.util.List[String] = new ArrayList()

  var configuration: ColumnKonfiguration = new ColumnKonfiguration()

  def getConfiguration(): ColumnKonfiguration = {
    configuration
  }
  def getBooks(): java.util.List[Book] = {
    books
  }
  def getColumns(): java.util.List[String] = {
    columns
  }
  def setConfiguration(c: ColumnKonfiguration) = {
    configuration = c
  }
  def setBooks(b: java.util.List[Book]) = {
    books = b
  }
  def setColumns(v: java.util.List[String]) = {
    columns = v
  }
}