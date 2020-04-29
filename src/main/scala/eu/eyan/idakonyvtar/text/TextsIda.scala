package eu.eyan.idakonyvtar.text

import java.io.File

import eu.eyan.idakonyvtar.IdaLibrary
import eu.eyan.log.Log
import eu.eyan.util.excel.ExcelPlus
import eu.eyan.util.text._
import rx.lang.scala.Observable
import rx.lang.scala.subjects.BehaviorSubject

class TextsIda extends Texts {
  def getLanguages = languages

  private lazy val languageInRegistry = IdaLibrary.registryValue(classOf[TextsIda].getName)
  private lazy val translationsXlsInputStream = ClassLoader.getSystemResourceAsStream("translations.xls")
  private lazy val translationsTable = ExcelPlus.readExcelFromStream(translationsXlsInputStream, "translations")
  private lazy val languages = translationsTable.firstRowCells.drop(2).filter(_.content.nonEmpty).map(_.content.get).filter(_.nonEmpty).toArray

  language.subscribe(_.foreach(languageInRegistry.save))

  protected def initialLanguage = languageInRegistry.read

  protected def getTextTranslation(technicalName: String, language: Option[String]) = {
    Log.debug(s"TechnicalName=$technicalName, language=$language")

    val translationColumnIndex = translationsTable.columnFromFirstRow(language.getOrElse("English"))
    Log.debug("Language col " + translationColumnIndex)

    val rowIndex = translationsTable.rowFromFirstColumn(technicalName)
    Log.debug("technicalName row " + rowIndex)

    val colAndRow = for (a <- translationColumnIndex; b <- rowIndex) yield (a, b)
    val translation = colAndRow.map(translationsTable.getCell).flatMap(_.content)
    Log.debug("translation " + translation)

    translation
  }

  private object IdaText {
    def apply(technicalName: String, args: Observable[Any]*) = new IdaText(technicalName, args: _*)
  }

  protected class IdaText(private val technicalName: String, private val args: Observable[Any]*) extends Text(noTranslation(technicalName), args: _*) {
    def optionGetOrElse(orElse: String)(option: Option[String]) = option.getOrElse(orElse)

    private val translateText = translate(technicalName)(_)
    private val translated = language map translateText
    private val validTranslationObs = translated map optionGetOrElse(noTranslation(technicalName))

    validTranslationObs subscribe setTemplate _
  }

  case object IdaLibraryTitleNoLibraryOpen extends IdaText("IdaLibraryTitleNoLibraryOpen")
  case object IdaLibraryTitleSingular { def apply(selectedLibrary: Observable[String]) = IdaText("IdaLibraryTitleSingular", selectedLibrary) } 
  case object IdaLibraryTitlePlural { def apply(selectedLibrary: Observable[String], nrOfBooks: Observable[Int]) = IdaText("IdaLibraryTitlePlural", selectedLibrary, nrOfBooks) }
  case object IdaLibraryTitleEmpty { def apply(selectedLibrary: Observable[String]) = IdaText("IdaLibraryTitleEmpty", selectedLibrary) }
  
  case object IdaLibraryTitleIcon extends IdaText("IdaLibraryTitleIcon")
  case object MenuFile extends IdaText("MenuFile")
  case object MenuFileLoad extends IdaText("MenuFileLoad")
  case object MenuFileLoadIcon extends IdaText("MenuFileLoadIcon")

  case object LoadFileWindowTitle extends IdaText("LoadFileWindowTitle")
  case object LoadFileWindowFileType extends IdaText("LoadFileWindowFileType")
  case object LoadFileWindowLoadButton extends IdaText("LoadFileWindowLoadButton")
  case object LoadFileWindowCancelButton extends IdaText("LoadFileWindowCancelButton")
  case object LoadFileTexts extends TextsDialogFileChooser(LoadFileWindowTitle, LoadFileWindowLoadButton, LoadFileWindowCancelButton, LoadFileWindowFileType)

  case object MenuFileSave extends IdaText("MenuFileSave")
  case object MenuFileSaveIcon extends IdaText("MenuFileSaveIcon")
  case object SaveFileWindowTitle extends IdaText("SaveFileWindowTitle")
  case object SaveFileWindowFileType extends IdaText("SaveFileWindowFileType")
  case object SaveFileWindowSaveButton extends IdaText("SaveFileWindowSaveButton")
  case object SaveFileWindowCancelButton extends IdaText("SaveFileWindowCancelButton")
  case object MenuFileSaveAs extends IdaText("MenuFileSaveAs")
  case object MenuFileSaveAsIcon extends IdaText("MenuFileSaveAsIcon")

  case object SaveAsFileWindowTitle extends IdaText("SaveAsFileWindowTitle")
  case object SaveAsFileWindowFileType extends IdaText("SaveAsFileWindowFileType")
  case object SaveAsFileWindowSaveButton extends IdaText("SaveAsFileWindowSaveButton")
  case object SaveAsFileWindowCancelButton extends IdaText("SaveAsFileWindowCancelButton")
  case object SaveAsFileTexts extends TextsDialogFileChooser(SaveAsFileWindowTitle, SaveAsFileWindowSaveButton, SaveAsFileWindowCancelButton, SaveAsFileWindowFileType)

  case object SaveAsOverwriteConfirmText { def apply(filename: File) = IdaText("SaveAsOverwriteConfirmText", BehaviorSubject(filename)) }
  case object SaveAsOverwriteConfirmWindowTitle extends IdaText("SaveAsOverwriteConfirmWindowTitle")
  case object SaveAsOverwriteYes extends IdaText("SaveAsOverwriteYes")
  case object SaveAsOverwriteNo extends IdaText("SaveAsOverwriteNo")

  case object SaveErrorWindowText extends IdaText("SaveErrorWindowText")
  case object SaveErrorWindowTitle extends IdaText("SaveErrorWindowTitle")
  case object SaveErrorWindowButton extends IdaText("SaveErrorWindowButton")
  case object SaveErrorTexts extends TextsDialogYes(SaveErrorWindowText, SaveErrorWindowTitle, SaveErrorWindowButton)

  case object LoadErrorWindowText extends IdaText("LoadErrorWindowText")
  case object LoadErrorWindowTitle extends IdaText("LoadErrorWindowTitle")
  case object LoadErrorWindowButton extends IdaText("LoadErrorWindowButton")
  case object LoadErrorTexts extends TextsDialogYes(LoadErrorWindowText, LoadErrorWindowTitle, LoadErrorWindowButton)

  case object CloseLibraryWindowConfirmQuestion { def apply(filename: String) = IdaText("CloseLibraryWindowConfirmQuestion", BehaviorSubject(filename)) }
  case object CloseLibraryWindowTitle { def apply(filename: String) = IdaText("CloseLibraryWindowTitle", BehaviorSubject(filename)) }
  case object CloseLibraryWindowYes extends IdaText("CloseLibraryWindowYes")
  case object CloseLibraryWindowNo extends IdaText("CloseLibraryWindowNo")
  case object CloseLibraryWindowCancel extends IdaText("CloseLibraryWindowCancel")
  case object CloseLibraryWindowTexts { def apply(filename: String) = new TextsDialogYesNoCancel(CloseLibraryWindowConfirmQuestion(filename), CloseLibraryWindowTitle(filename), CloseLibraryWindowYes, CloseLibraryWindowNo, CloseLibraryWindowCancel) }

  case object ExitSaveLibraryWindowConfirmQuestion { def apply(filename: String) = IdaText("ExitSaveLibraryWindowConfirmQuestion", BehaviorSubject(filename)) }
  case object ExitSaveLibraryWindowTitle { def apply(filename: String) = IdaText("ExitSaveLibraryWindowTitle", BehaviorSubject(filename)) }
  case object ExitSaveLibraryWindowYes extends IdaText("ExitSaveLibraryWindowYes")
  case object ExitSaveLibraryWindowNo extends IdaText("ExitSaveLibraryWindowNo")
  case object ExitSaveLibraryWindowCancel extends IdaText("ExitSaveLibraryWindowCancel")
  case object ExitSaveLibraryTexts { def apply(filename: String) = new TextsDialogYesNoCancel(ExitSaveLibraryWindowConfirmQuestion(filename), ExitSaveLibraryWindowTitle(filename), ExitSaveLibraryWindowYes, ExitSaveLibraryWindowNo, ExitSaveLibraryWindowCancel) }

  case object MenuFileExit extends IdaText("MenuFileExit")
  case object MenuFileExitIcon extends IdaText("MenuFileExitIcon")

  case object ExitWindowTitle extends IdaText("ExitWindowTitle")
  case object ExitWindowConfirmQuestion extends IdaText("ExitWindowConfirmQuestion")
  case object ExitWindowYes extends IdaText("ExitWindowYes")
  case object ExitWindowNo extends IdaText("ExitWindowNo")
  case object ExitWindowTexts extends TextsDialogYesNo(ExitWindowConfirmQuestion, ExitWindowTitle, ExitWindowYes, ExitWindowNo)

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
  case object CopyLogsTexts extends TextsDialogYes(CopyLogsWindowText, CopyLogsWindowTitle, CopyLogsWindowButton)

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
  case object AboutWindowTexts extends TextsDialogYes(AboutWindowText, AboutWindowTitle, AboutWindowButton)

  case object ToolbarTitle extends IdaText("ToolbarTitle")
  case object ToolbarSaveButton extends TextsButton(IdaText("ToolbarSaveButton"), IdaText("ToolbarSaveButtonTooltip"), IdaText("ToolbarSaveButtonIcon"))
  case object ToolbarLoadButton extends TextsButton(IdaText("ToolbarLoadButton"), IdaText("ToolbarLoadButtonTooltip"), IdaText("ToolbarLoadButtonIcon"))
  case object ToolbarNewBookButton extends TextsButton(IdaText("ToolbarNewBookButton"), IdaText("ToolbarNewBookButtonTooltip"), IdaText("ToolbarNewBookButtonIcon"))
  case object ToolbarDeleteBookButton extends TextsButton(IdaText("ToolbarDeleteBookButton"), IdaText("ToolbarDeleteBookButtonTooltip"), IdaText("ToolbarDeleteBookButtonIcon"))

  case object DeleteBookWindowTitle extends IdaText("DeleteBookWindowTitle")
  case object DeleteBookWindowQuestion extends IdaText("DeleteBookWindowQuestion")
  case object DeleteBookWindowYes extends IdaText("DeleteBookWindowYes")
  case object DeleteBookWindowNo extends IdaText("DeleteBookWindowNo")
  case object DeleteBookWindowTexts extends TextsDialogYesNo(DeleteBookWindowQuestion, DeleteBookWindowTitle, DeleteBookWindowYes, DeleteBookWindowNo)

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
  case object EditBookWindowTitle { def apply(bookTitle: Observable[String]) = IdaText("EditBookWindowTitle", bookTitle) }
  case object EditBookSaveButton extends IdaText("EditBookSaveButton")
  case object EditBookCancelButton extends IdaText("EditBookCancelButton")
  case object ConfigTitleFieldName extends IdaText("ConfigTitleFieldName")

  case object ConfigMultiField extends IdaText("ConfigMultiField")
  case object ConfigAutocomplete extends IdaText("ConfigAutocomplete")
  case object ConfigMarcCode extends IdaText("ConfigMarcCode")
  case object ConfigInTable extends IdaText("ConfigInTable")
  case object ConfigRemember extends IdaText("ConfigRemember")
  case object ConfigPicture extends IdaText("ConfigPicture")
  case object ConfigYes extends IdaText("ConfigYes")

}