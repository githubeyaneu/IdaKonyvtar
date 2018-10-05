package eu.eyan.idakonyvtar.model

import com.google.common.collect.Lists
import java.io.File

class Library(
    val file: File,
    val configuration: ColumnKonfiguration,
    val columns: List[String],
    val books: java.util.List[Book] = Lists.newArrayList()) /* java.util required for LibraryModel */
