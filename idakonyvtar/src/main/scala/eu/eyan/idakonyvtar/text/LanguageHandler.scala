package eu.eyan.idakonyvtar.text

import rx.lang.scala.Observable
import rx.lang.scala.subjects.BehaviorSubject
import eu.eyan.util.scala.Try
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.idakonyvtar.util.ExcelHandler
import eu.eyan.log.Log
import eu.eyan.util.text.Text
import eu.eyan.util.registry.RegistryPlus
import eu.eyan.idakonyvtar.IdaLibrary
import javax.swing.JFrame
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit
import eu.eyan.util.swing.Alert
import javax.swing.JComboBox

// TODO write tests
object LanguageHandler {
  val NO = "Nem"
  val TITLE = "IdaKönyvtár"
  val YES = "Igen"
  val NO_BOOK_FOR_THE_FILTER = "Ilyen szűrőfeltételekkel nem található book."
  val NO_BOOK_IN_THE_LIST = "Nincs book a listában."
  val ERROR_AT_READING = "Hiba a beolvasáskor"

}

class LanguageHandler(registryName: String) { // TODO rename LanguageHandler

  // TODO make a Registry class (not object) to remove code duplication
  // TODO make a RegistryParam class (not object) to remove code duplication
  lazy val registryLanguageParameter = LanguageHandler.getClass.getName

  lazy val translationsXls = "translations.xls".toResourceFile.get
  lazy val translationsTable = ExcelHandler.readExcel(translationsXls, "translations")
  lazy val languages = translationsTable.row(0).drop(2).filter(_.nonEmpty)

  lazy val initialLanguage = readStoredLanguageFromRegistry orElse selectLanguageByDialog

  lazy val usedLanguage = {
    initialLanguage foreach saveSelectedLanguageInRegistry
    initialLanguage getOrElse "Magyar"
  }

  lazy val language = BehaviorSubject[String](usedLanguage)

  def readStoredLanguageFromRegistry = RegistryPlus.readOption(registryName, registryLanguageParameter)
  def saveSelectedLanguageInRegistry(selectedLanguage: String) = RegistryPlus.write(registryName, registryLanguageParameter, selectedLanguage)

  def selectLanguageByDialog = Alert.alertOptions("Please select your language!", languages.toArray)

  def getTextTranslation(technicalName: String, language: String) = {
    Log.debug(s"TechnicalName=$technicalName, language=$language")
    val translationColumnIndex = translationsTable.columnIndex(language)
    Log.debug("Language col " + translationColumnIndex)
    val rowIndex = translationsTable.rowIndex(technicalName)
    Log.debug("technicalName row " + rowIndex)
    if (translationColumnIndex.nonEmpty && rowIndex.nonEmpty) translationsTable.cells.get((translationColumnIndex.get, rowIndex.get))
    else None
  }

  def onLanguageSelected(selectedLanguage: String) = {
    Log.info(selectedLanguage)
    language.onNext(selectedLanguage)
    saveSelectedLanguageInRegistry(selectedLanguage)
    this
  }

  abstract class IdaText(private val txt: String, private val args: Observable[Any]*) extends Text(BehaviorSubject(txt), args: _*) {
    def translate(language: String) = getTextTranslation(this.getClass.getSimpleName.replace("$", ""), language)
    lazy val templateTranslated = language map translate
    def validTranslation(opt: Option[String]) = opt.nonEmpty && opt.get.nonEmpty
    lazy val onlyValidTranslations = templateTranslated.filter(validTranslation).map(_.get)
    onlyValidTranslations.subscribe(template)

    def get = {
      Log.debug("txt: " + txt)
      txt
    }
  }

  case object TextMenuLanguages extends IdaText("Languages")
  case object TextExitWindowConfirmQuestion extends IdaText("Do you really want to quit?")

  //case class TextIdaLibraryTitle(nrOfBooks: Observable[Int]) extends IdaText("IdaLibrary - %s books.", nrOfBooks)
  //case object TextMenuDebug extends IdaText("Debug")
  //case object TextMenuDebugLogWindow extends IdaText("Log Window")
  //case object TextMenuDebugCopyLogs extends IdaText("Copy logs")
  //case object TextMenuDebugClearRegistry extends IdaText("Clear registry")
  case class TextIdaLibraryTitle(nrOfBooks: Observable[Int]) extends IdaText("IDALIBRARY", nrOfBooks)
  case object TextMenuDebug extends IdaText("DBG")
  case object TextMenuDebugLogWindow extends IdaText("LOGWIND")
  case object TextMenuDebugCopyLogs extends IdaText("CPYLOG")
  case object TextMenuDebugClearRegistry extends IdaText("CLRREG")
}



