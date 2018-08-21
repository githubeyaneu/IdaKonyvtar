package eu.eyan.idakonyvtar.text

import rx.lang.scala.Observable
import rx.lang.scala.subjects.BehaviorSubject
import eu.eyan.util.scala.Try
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.idakonyvtar.util.ExcelHandler
import eu.eyan.log.Log

// TODO write tests
object Texts {
  val NO = "Nem"
  val TITLE = "IdaKönyvtár"
  val YES = "Igen"
  val NO_BOOK_FOR_THE_FILTER = "Ilyen szűrőfeltételekkel nem található book."
  val NO_BOOK_IN_THE_LIST = "Nincs book a listában."
  val ERROR_AT_READING = "Hiba a beolvasáskor"

  lazy val translationsXls = "translations.xls".toResourceFile.get
  lazy val translationsTable = ExcelHandler.readExcel(translationsXls, "translations")

  def getTextTranslation(technicalName: String) = {
    Log.debug("technicalName: " + technicalName)
    val translationColumnIndex = translationsTable.columnIndex("Magyar")
    Log.debug("Magyar col " + translationColumnIndex)
    val rowIndex = translationsTable.rowIndex(technicalName)
    Log.debug("technicalName row " + rowIndex)
    if (translationColumnIndex.nonEmpty && rowIndex.nonEmpty) translationsTable.cells.get((translationColumnIndex.get, rowIndex.get))
    else None
  }
}

sealed abstract class TextN(val txt: String, args: Observable[Any]*) extends Observable[String] {
  private val textObservable = BehaviorSubject[String]()

  if (args.nonEmpty) {

    // TODO check string and param numbers
    def toText(params: List[Any]) = Try(String.format(get, params.map(_.asInstanceOf[Object]): _*)).getOrElse("")
    val observablesCombined = Observable.combineLatest(args.toIterable)(_.toList).map(toText)
    observablesCombined.subscribe(textObservable)
  }
  val asJavaObservable: rx.Observable[_ <: String] = textObservable.asJavaObservable

  def get = {
    Log.debug("txt: " + txt)
    val translation = Texts.getTextTranslation(this.getClass.getSimpleName.replace("$", ""))
    Log.debug("translation " + translation)
    val template = translation.getOrElse(txt)
    Log.debug("template " + template)
    template
  }
}

case object TextExitWindowConfirmQuestion extends TextN("Do you really want to quit?")
case class TextIdaLibraryTitle(nrOfBooks: Observable[Int]) extends TextN("IdaLibrary - %s books.", nrOfBooks)
