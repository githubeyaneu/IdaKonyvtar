package eu.eyan.idakonyvtar.model

import com.google.common.collect.Lists

class Library(val configuration: ColumnKonfiguration, val columns: Seq[String], var books: java.util.List[Book] = Lists.newArrayList() /* java.util required for LibraryModel*/ )