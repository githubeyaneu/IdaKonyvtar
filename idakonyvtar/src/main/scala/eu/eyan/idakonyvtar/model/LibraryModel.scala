package eu.eyan.idakonyvtar.model;

import com.jgoodies.binding.list.SelectionInList;

class LibraryModel(
    val books: SelectionInList[Book] = new SelectionInList[Book](),
    var library: Library = null) /* var, because of loading other library */
