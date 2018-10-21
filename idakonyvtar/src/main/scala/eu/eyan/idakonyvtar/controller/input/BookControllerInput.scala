package eu.eyan.idakonyvtar.controller.input;

import eu.eyan.idakonyvtar.model.Book;
import eu.eyan.idakonyvtar.model.FieldConfiguration;
import java.awt.Image
import scala.collection.mutable.Map
import java.io.File

class BookControllerInput(
    val book: Book,
    val columns: List[String],
    val columnConfiguration: FieldConfiguration,
    val bookList: List[Book],
    val isbnEnabled: Boolean = false,
    val loadedFile: File
)
