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
import eu.eyan.util.text.Texts
import eu.eyan.util.registry.RegistryValue

// TODO write tests
class TextsIda(registryName: String, defaultLanguage: Array[String]=> Option[String]) extends Texts {
  lazy val registryValue = RegistryValue(registryName, classOf[TextsIda].getName)

  lazy val translationsXls = "translations.xls".toResourceFile.get
  lazy val translationsTable = ExcelHandler.readExcel(translationsXls, "translations")
  lazy val languages = translationsTable.row(0).drop(2).filter(_.nonEmpty)
  lazy val initialLanguage = registryValue.read orElse defaultLanguage(languages.toArray)

  lazy val getAndSaveLanguage = {
    initialLanguage foreach registryValue.save
    initialLanguage getOrElse "Magyar"
  }

  
  
  lazy val language = BehaviorSubject[String](getAndSaveLanguage)

  

  def getTextTranslation(technicalName: String, language: String) = {
    Log.debug(s"TechnicalName=$technicalName, language=$language")
    val translationColumnIndex = translationsTable.columnIndex(language)
    Log.debug("Language col " + translationColumnIndex)
    val rowIndex = translationsTable.rowIndex(technicalName)
    Log.debug("technicalName row " + rowIndex)
    val translation = if (translationColumnIndex.nonEmpty && rowIndex.nonEmpty) {
      val option = translationsTable.cells.get((translationColumnIndex.get, rowIndex.get))
      if (option.nonEmpty && option.get.nonEmpty) option
      else None
    } else
      None
    Log.debug("translation " + translation)

    translation
  }

  def onLanguageSelected(selectedLanguage: String) = {
    Log.info(selectedLanguage)
    language.onNext(selectedLanguage)
    registryValue.save(selectedLanguage)
    this
  }

  def translate(technicalName: String)(language: String) = getTextTranslation(technicalName, language) // this.getClass.getSimpleName.replace("$", "")
  def optionGetOrElse(orElse: String)(option: Option[String]) = option.getOrElse(orElse)
  def noTranslation(technicalName: String) = s"**$technicalName**"

  protected class IdaText(private val technicalName: String, private val args: Observable[Any]*) extends Text(BehaviorSubject(noTranslation(technicalName)), args: _*) {
    lazy val translateText = translate(technicalName)(_)
    lazy val translated = language map translateText
    lazy val validTranslationObs = translated map optionGetOrElse(noTranslation(technicalName))
    validTranslationObs subscribe template
  }
  ///////////////////////////////////////////////////////////////////////////////////////////////

  case object IdaLibraryTitleSingular extends IdaText("IdaLibraryTitleSingular")
  case object IdaLibraryTitlePlural { def apply(nrOfBooks: Observable[Int]) = new IdaText("IdaLibraryTitlePlural", nrOfBooks) }
  case object IdaLibraryTitleEmpty extends IdaText("IdaLibraryTitleEmpty")
  case object IdaLibraryTitleIcon extends IdaText("IdaLibraryTitleIcon")
  case object MenuFile extends IdaText("MenuFile")
  case object MenuFileLoad extends IdaText("MenuFileLoad")
  case object MenuFileLoadIcon extends IdaText("MenuFileLoadIcon")
  case object LoadFileWindowTitle extends IdaText("LoadFileWindowTitle")
  case object LoadFileWindowFileType extends IdaText("LoadFileWindowFileType")
  case object MenuFileSave extends IdaText("MenuFileSave")
  case object MenuFileSaveIcon extends IdaText("MenuFileSaveIcon")
  case object SaveFileWindowTitle extends IdaText("SaveFileWindowTitle")
  case object SaveFileWindowFileType extends IdaText("SaveFileWindowFileType")
  case object MenuFileExit extends IdaText("MenuFileExit")
  case object MenuFileExitIcon extends IdaText("MenuFileExitIcon")
  case object ExitWindowTitle extends IdaText("ExitWindowTitle")
  case object ExitWindowConfirmQuestion extends IdaText("ExitWindowConfirmQuestion")
  case object ExitWindowYes extends IdaText("ExitWindowYes")
  case object ExitWindowNo extends IdaText("ExitWindowNo")
  case object MenuLanguages extends IdaText("MenuLanguages")
  case object MenuLanuagesIcon extends IdaText("MenuLanuagesIcon")
  case object MenuDebug extends IdaText("MenuDebug")
  case object MenuDebugLogWindow extends IdaText("MenuDebugLogWindow")
  case object MenuDebugLogWindowIcon extends IdaText("MenuDebugLogWindowIcon")
  case object DebugWindowTitle extends IdaText("DebugWindowTitle")
  case object DebugWindowDeleteButton extends IdaText("DebugWindowDeleteButton")
  case object DebugWindowLogLevelLabel extends IdaText("DebugWindowLogLevelLabel")
  case object DebugWindowLogLevelNone extends IdaText("DebugWindowLogLevelNone")
  case object DebugWindowLogLevelFatal extends IdaText("DebugWindowLogLevelFatal")
  case object DebugWindowLogLevelError extends IdaText("DebugWindowLogLevelError")
  case object DebugWindowLogLevelWarn extends IdaText("DebugWindowLogLevelWarn")
  case object DebugWindowLogLevelInfo extends IdaText("DebugWindowLogLevelInfo")
  case object DebugWindowLogLevelDebug extends IdaText("DebugWindowLogLevelDebug")
  case object DebugWindowLogLevelTrace extends IdaText("DebugWindowLogLevelTrace")
  case object DebugWindowMaxRowsLabel extends IdaText("DebugWindowMaxRowsLabel")
  case object DebugWindowLogsLabel extends IdaText("DebugWindowLogsLabel")
  case object DebugWindowConsoleOutLabel extends IdaText("DebugWindowConsoleOutLabel")
  case object DebugWindowConsoleErrLabel extends IdaText("DebugWindowConsoleErrLabel")
  case object MenuDebugCopyLogs extends IdaText("MenuDebugCopyLogs")
  case object MenuDebugCopyLogsIcon extends IdaText("MenuDebugCopyLogsIcon")
  case object CopyLogsWindowTitle extends IdaText("CopyLogsWindowTitle")
  case object CopyLogsWindowText extends IdaText("CopyLogsWindowText")
  case object CopyLogsWindowButton extends IdaText("CopyLogsWindowButton")
  case object MenuDebugClearRegistry extends IdaText("MenuDebugClearRegistry")
  case object MenuDebugClearRegistryIcon extends IdaText("MenuDebugClearRegistryIcon")
  case object MenuHelp extends IdaText("MenuHelp")
  case object MenuHelpEmailError extends IdaText("MenuHelpEmailError")
  case object MenuHelpEmailErrorIcon extends IdaText("MenuHelpEmailErrorIcon")
  case object EmailErrorWindowTitle extends IdaText("EmailErrorWindowTitle")
  case object EmailErrorWindowText extends IdaText("EmailErrorWindowText")
  case object EmailErrorWindowButton extends IdaText("EmailErrorWindowButton")
  case object EmailErrorEmailTo extends IdaText("EmailErrorEmailTo")
  case object EmailErrorEmailSubject extends IdaText("EmailErrorEmailSubject")
  case object EmailErrorEmailBody extends IdaText("EmailErrorEmailBody")
  case object MenuHelpAbout extends IdaText("MenuHelpAbout")
  case object MenuHelpAboutIcon extends IdaText("MenuHelpAboutIcon")
  case object AboutWindowTitle extends IdaText("AboutWindowTitle")
  case object AboutWindowText extends IdaText("AboutWindowText")
  case object AboutWindowButton extends IdaText("AboutWindowButton")
  case object ToolbarTitle extends IdaText("ToolbarTitle")
  case object ToolbarSaveButton extends IdaText("ToolbarSaveButton")
  case object ToolbarSaveButtonIcon extends IdaText("ToolbarSaveButtonIcon")
  case object ToolbarSaveButtonTooltip extends IdaText("ToolbarSaveButtonTooltip")
  case object ToolbarLoadButton extends IdaText("ToolbarLoadButton")
  case object ToolbarLoadButtonIcon extends IdaText("ToolbarLoadButtonIcon")
  case object ToolbarLoadButtonTooltip extends IdaText("ToolbarLoadButtonTooltip")
  case object ToolbarNewBookButton extends IdaText("ToolbarNewBookButton")
  case object ToolbarNewBookButtonIcon extends IdaText("ToolbarNewBookButtonIcon")
  case object ToolbarNewBookButtonTooltip extends IdaText("ToolbarNewBookButtonTooltip")
  case object ToolbarDeleteBookButton extends IdaText("ToolbarDeleteBookButton")
  case object ToolbarDeleteBookButtonIcon extends IdaText("ToolbarDeleteBookButtonIcon")
  case object ToolbarDeleteBookButtonTooltip extends IdaText("ToolbarDeleteBookButtonTooltip")
  case object DeleteBookWindowTitle extends IdaText("DeleteBookWindowTitle")
  case object DeleteBookWindowQuestion extends IdaText("DeleteBookWindowQuestion")
  case object DeleteBookWindowYes extends IdaText("DeleteBookWindowYes")
  case object DeleteBookWindowNo extends IdaText("DeleteBookWindowNo")
  case object ToolbarFilterLabel extends IdaText("ToolbarFilterLabel")
  case object EmptyLibrary extends IdaText("EmptyLibrary")
  case object NoResultAfterFilter extends IdaText("NoResultAfterFilter")
  case object NewBookWindowTitle extends IdaText("NewBookWindowTitle")
  case object NewBookSaveButton extends IdaText("NewBookSaveButton")
  case object NewBookCancelButton extends IdaText("NewBookCancelButton")
  case object NewBookCancelWindowTitle extends IdaText("NewBookCancelWindowTitle")
  case object NewBookCancelConfirmQuestion extends IdaText("NewBookCancelConfirmQuestion")
  case object NewBookCancelYes extends IdaText("NewBookCancelYes")
  case object NewBookCancelNo extends IdaText("NewBookCancelNo")
  case object NewBookIsbnLabel extends IdaText("NewBookIsbnLabel")
  case object NewBookIsbnSearching extends IdaText("NewBookIsbnSearching")
  case object NewBookIsbnSearchNoResult extends IdaText("NewBookIsbnSearchNoResult")
  case object NewBookIsbnSearchSuccess extends IdaText("NewBookIsbnSearchSuccess")
  case object NewBookDataLabel extends IdaText("NewBookDataLabel")
  case object NewBookImagesLabel extends IdaText("NewBookImagesLabel")
  case object NewBookWebcamLabel extends IdaText("NewBookWebcamLabel")
  case object AutocompleteFieldBackgroundTitle extends IdaText("AutocompleteFieldBackgroundTitle")
  case object AutocompleteFieldTooltip extends IdaText("AutocompleteFieldTooltip")
  case object AutocompleteFieldDeleteButton extends IdaText("AutocompleteFieldDeleteButton")
  case object WebcamTakeImageButton extends IdaText("WebcamTakeImageButton")
  case object ShowImageWindowTitle extends IdaText("ShowImageWindowTitle")
  case object EditBookWindowTitle extends IdaText("EditBookWindowTitle")
}