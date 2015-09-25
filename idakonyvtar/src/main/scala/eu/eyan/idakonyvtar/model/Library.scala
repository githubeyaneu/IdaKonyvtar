package eu.eyan.idakonyvtar.model

import java.util.ArrayList

import scala.collection.mutable.MutableList

class Library(
  var books: java.util.List[Book] = new ArrayList() // java.util required for LibraryModel
  , var configuration: ColumnKonfiguration = new ColumnKonfiguration(), var columns: MutableList[String] = MutableList())