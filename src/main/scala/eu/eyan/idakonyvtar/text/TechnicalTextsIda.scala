package eu.eyan.idakonyvtar.text

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

object TechnicalTextsIda {
  // TODO: how to handle it? maven etc...
  val VERSION = "1.1.3"

  val LIBRARYXLS = "library.xls"

  val LANGUAGE_SELECTION = "Language selection"
  val PLEASE_SELECT_YOUR_LANGUAGE = "Please select your language!"

  val UTF8 = "utf-8"

  val WRITE_EMAIL = "mailto:idalibrary@eyan.hu?subject=IdaLibrary%20error&body="

  val PLUS_AS_REGEX = "\\+"
  val SPACE_URL_ENCODED = "%20"

  val LAST_LOAD_DIRECTORY = "lastLoadDirectory"
  val LAST_LOADED_FILES = "lastLoadedFiles"
  val LAST_ACTIVE_FILE = "lastActiveFile"

  val EMPTY_STRING = ""
  val COLON_SPACE = ": "
  val IMAGES_DIR_POSTFIX = ".images"
  val DEFAULT_IMAGE_NAME = "\\IMG.JPG"
  val IMAGE_EXTENSION = "JPG"

  val DIRTY = " *"
  val NOT_DIRTY = "  "

  val PARAM = "%s"

  val LOCAL_DIR = "."

  val XLS = "xls"

  val BASIC_FUNCTIONS = "Alapfunkciók"

  val SAVE_LIBRARY = "Library mentése"
  val LOAD_LIBRARY = "Library betöltése"
  val ADD_NEW_BOOK = "Új book hozzáadása"
  val DELETE_BOOK = "Book törlése"

  val FILTER = "filter"

  val ERROR_AT_READING_LIBRARY = "Hiba a beolvasáskor"

  val ISBN_TEXT = "isbnText"
  val ISBN_LABEL = "isbnLabel"

  val SAVE_BUTTON_NAME = "Save"
  val CANCEL_BUTTON_NAME = "Cancel"

  val MULTIFIELD_SEPARATOR = " + "
  val MULTIFIELS_SEPARATOR_REGEX = MULTIFIELD_SEPARATOR.replace("+", "\\+")

  val DATE_TIME_FORMAT = "yyyy-MM-dd_HH-mm-ss"
  val FILE_SEPARATOR = File.separator

  private val DATE_TIME_FORMATTER = new SimpleDateFormat(DATE_TIME_FORMAT)
  private def NOW = new Date()
  def NOW_DATE_TIME = DATE_TIME_FORMATTER.format(NOW)
  def BACKUP_FILE_TEMPLATE(dir:String, sourceFileName:String) = s"$dir$FILE_SEPARATOR${sourceFileName}_backup_v${VERSION}_$NOW_DATE_TIME.zip"
  
  val EXCEL_SHEET_NAME_COLUMN_CONFIGURATION = "Configuration"
  val EXCEL_SHEET_NAME_BOOKS = "Books"
  
  val NO_COLUMNS_FOUND = "No columns to show(!) in library"
  
  val MARC_CODES_SEPARATOR = ","
  val MARC_CODE_SEPARATOR = "-"
}