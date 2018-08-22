package eu.eyan.idakonyvtar.text

import rx.lang.scala.Observable
import rx.lang.scala.subjects.BehaviorSubject
import eu.eyan.util.scala.Try
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.idakonyvtar.util.ExcelHandler
import eu.eyan.log.Log
import eu.eyan.util.text.Text

// TODO write tests
object TextIdaLibrary {
  val NO = "Nem"
  val TITLE = "IdaKönyvtár"
  val YES = "Igen"
  val NO_BOOK_FOR_THE_FILTER = "Ilyen szűrőfeltételekkel nem található book."
  val NO_BOOK_IN_THE_LIST = "Nincs book a listában."
  val ERROR_AT_READING = "Hiba a beolvasáskor"

  lazy val translationsXls = "translations.xls".toResourceFile.get
  lazy val translationsTable = ExcelHandler.readExcel(translationsXls, "translations")
  lazy val languages = translationsTable.row(0).drop(2).filter(_.nonEmpty)

  lazy val language = BehaviorSubject[String]("")

  def getTextTranslation(technicalName: String) = {
    Log.debug("technicalName: " + technicalName)
    val translationColumnIndex = translationsTable.columnIndex("Magyar")
    Log.debug("Language col " + translationColumnIndex)
    val rowIndex = translationsTable.rowIndex(technicalName)
    Log.debug("technicalName row " + rowIndex)
    if (translationColumnIndex.nonEmpty && rowIndex.nonEmpty) translationsTable.cells.get((translationColumnIndex.get, rowIndex.get))
    else None
  }

  def onLanguageSelected(selectedLanguage: String) = {
    Log.info(selectedLanguage)
    language.onNext(selectedLanguage)
    this
  }
}

abstract class IdaText(private val txt: String, private val args: Observable[Any]*) extends Text(BehaviorSubject(txt), args: _*) {
  def translate = TextIdaLibrary.getTextTranslation(this.getClass.getSimpleName.replace("$", ""))

  def get = {
    Log.debug("txt: " + txt)
    val translation = translate
    Log.debug("translation " + translation)
    val template = translation.getOrElse(txt)
    Log.debug("template " + template)
    template
  }
}

case object TextMenuLanguages extends IdaText("Languages")
case object TextExitWindowConfirmQuestion extends IdaText("Do you really want to quit?")
case class TextIdaLibraryTitle(nrOfBooks: Observable[Int]) extends IdaText("IdaLibrary - %s books.", nrOfBooks)

//case object TextMenuDebug extends IdaText("Debug")
//case object TextMenuDebugLogWindow extends IdaText("Log Window")
//case object TextMenuDebugCopyLogs extends IdaText("Copy logs")
//case object TextMenuDebugClearRegistry extends IdaText("Clear registry")
case object TextMenuDebug extends IdaText("DBG")
case object TextMenuDebugLogWindow extends IdaText("LOGWIND")
case object TextMenuDebugCopyLogs extends IdaText("CPYLOG")
case object TextMenuDebugClearRegistry extends IdaText("CLRREG")