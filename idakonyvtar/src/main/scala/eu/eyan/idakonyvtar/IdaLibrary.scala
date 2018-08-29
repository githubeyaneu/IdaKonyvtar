package eu.eyan.idakonyvtar

import java.io.File

import com.google.common.io.Resources

import eu.eyan.idakonyvtar.controller.LibraryController
import eu.eyan.idakonyvtar.controller.input.LibraryControllerInput
import eu.eyan.idakonyvtar.text.LanguageHandler._
import eu.eyan.log.Log
import eu.eyan.log.LogWindow
import eu.eyan.util.awt.AwtHelper.onWindowClosing
import eu.eyan.idakonyvtar.util.WebCam
import javax.swing.JMenuBar
import org.jdesktop.swingx.JXFrame
import javax.swing.JToolBar
import eu.eyan.idakonyvtar.controller.IController
import java.awt.Component
import java.awt.BorderLayout
import javax.swing.JFrame
import java.awt.Frame
import eu.eyan.idakonyvtar.controller.IControllerWithMenu
import eu.eyan.util.swing.JFramePlus.JFramePlusImplicit
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit
import javax.swing.WindowConstants
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.util.awt.clipboard.ClipboardPlus
import eu.eyan.util.swing.Alert
import eu.eyan.util.registry.RegistryPlus
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import eu.eyan.idakonyvtar.view.LibraryMenuAndToolBar
import eu.eyan.idakonyvtar.text.LanguageHandler
import rx.lang.scala.subjects.BehaviorSubject
import eu.eyan.util.string.StringPlus.StringPlusImplicit

object IdaLibrary {

  // TODO - összes java test megszüntetése
  // TODO - tabs for more libraries
  // TODO - tabok bezárása
  // TODO - changed * for the libs and by close show not saved and so on.
  // TODO - Fájl menü library betöltése -> Könyvtár vagy fájl
  // TODO - Fájl menü library mentése -> Könyvtár vagy fájl
  // TODO - Fájl menü debug -> ...
  // TODO - Fájl menü kilépés
  // TODO - menü gombok feliratok magyarosítása
  // TODO - menü gombok Törlés -> Könyv törlése
  // TODO - nyelv kezelése rendesen - menü - xls
  // TODO - menu - hiba - open log windows , copy logs to clipboard, clear registry values(?)
  // TODO - menu - Help - about, write email, ötlet, javaslat,
  // TODO - menu - Help - about leírás: felelöösségvállalás oszk stb
  // TODO - menu - Help - seg<tség a konfigurációhoz
  // TODO - menü gombok több köztes hely
  // TODO - Mentés -> kiválasztás nélkül
  // TODO - Mentés másként -> legyen menjen oda ahol van
  // TODO - Töltés: menjen oda ahonnan a legutolsót töltötte
  // TODO - program indítás: emlékezzen az összes nyitott fájlra mint a npp -> registry
  // TODO - új könyv: isbn legyen az alap
  // TODO - új könyv: fentre rendezve minden
  // TODO - új könyv: ha lehet jframe maximalizálva
  // TODO - új könyv: mentés gomb kicsi
  // TODO - mentse el a legutolsó kiválsztott webcamet
  // TODO - több webcam kiesik bejön -> legyen rendesen kezelve hibaüzenetekkel(?)
  // TODO - új könyv: képek ha nincs, akkor is kép hogy nincs
  // TODO - új könyv: képek címfelirat
  // TODO - új könyv: képek fentre rendezve
  // TODO - új könyv: webcam preview
  // TODO - új könyv: képek ahány kép, annyi férjen ki átméretezésnél képek átméretezése
  // TODO - új könyv: mégsem gomb fölöslegesen széles
  // TODO - új könyv: kép Katt átnevezése
  // TODO - új könyv: kép legördülöö vagy autocomplete, frissítse a képet
  // TODO - új könyv: fent - nem kell a címsorban
  // TODO - képek: több ktárba, mert több ezer fájl lehetséges
  // TODO - könyv szerkesztése címsor az összes megjelenített oszlop
  // TODO - új könyv - escape close ha nincs változás
  // TODO - új könyv - escape kérdés ha változás
  // TODO - könyv szerkesztése - escape close ha nincs változás
  // TODO - könyv szerkesztése - escape kérdés ha változás
  // TODO - könyv szerkesztése - autocomplete törlés szebb ikon
  // TODO - könyv szerkesztése - autocomplete felirat magyarosítása - lehet automatikusan ? -> observable!
  // TODO - refactoring - kill mvc  ha van értelme
  // TODO - verziószám kezelése... -> maven fordítsa be, menjen a logba is.
  // TODO - default ktár jobb szebb
  // TODO - angol német adatbázis?
  // TODO - második magyar adatbázis
  // TODO - marc keresés több eredmény kezelése kis gombokkal felülírható az eredeti

  val DEFAULT_LIBRARY = "library.xls"
  val VERSION = "1.1.1"

  def main(args: Array[String]): Unit = {
    val path = if (args.isEmpty || args(0) == null) DEFAULT_LIBRARY; else args(0)

    val file = new File(path)

    //TODO refact...
    val fileToOpen =
      if (file.exists()) file
      else path.toResourceFile.get

    Log.activateDebugLevel
    Log.info("Resource -> File: " + fileToOpen)

    new IdaLibrary().startLibrary(fileToOpen)
  }
}

class IdaLibrary {
  def startLibrary(fileToOpen: File) = {
    val controller = new LibraryController
    val toolBar = controller.getToolBar

    controller.initData(new LibraryControllerInput(fileToOpen))

    lazy val frame: JFrame = new JFrame()
    lazy val texts = new LanguageHandler(IdaLibrary.getClass.getName)

    def confirmExit(frame: JFrame) = DialogHelper.yesNo(frame, texts.TextExitWindowConfirmQuestion.get, "Megerősítés")
    def closeFrame(frame: JFrame) = frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));

    def getAllLogs = {
      Alert.alert("Kérem ellenőrizze az adatokat. A naplófájlok tartalmazhatnak személyes információkat, pl. könyvek adatai.")
      val loggerLogs = LogWindow.getAllLogs
      loggerLogs
    }

    def showAbout = Alert.alert("Személyes használatra. A készítő nem vállal felelősséget a program használatából eredő károkért. A program külső forrásból próbálhat adatokat gyűjteni ennek a következményeiért is a felhasználó a felelős.")

    def writeEmail =
      Desktop.getDesktop.mail(new URI("mailto:idalibrary@eyan.hu?subject=IdaLibrary%20error&body=" + URLEncoder.encode(getAllLogs, "utf-8").replace("+", "%20")))

    // TODO observable stringContext? : https://docs.scala-lang.org/overviews/core/string-interpolation.html

    frame
      .name(IdaLibrary.getClass.getName) // TODO refact dont use the same text for name and title
      .title(texts.TextIdaLibraryTitle(controller.numberOfBooks))
      .iconFromChar('I')
      .addFluent(toolBar, BorderLayout.NORTH)
      .addFluent(controller.getView, BorderLayout.CENTER)
      .menuItem(LibraryMenuAndToolBar.FILE, LibraryMenuAndToolBar.LOAD_LIBRARY, controller.loadLibrary)
      .menuItem(LibraryMenuAndToolBar.FILE, LibraryMenuAndToolBar.SAVE_LIBRARY, controller.saveLibrary)
      .menuItemSeparator(LibraryMenuAndToolBar.FILE)
      .menuItemEvent(LibraryMenuAndToolBar.FILE, "Kilépés", closeFrame)
      .menuItems("Nyelv", texts.languages, texts.onLanguageSelected)
      .menuItemEvent("Debug", "Napló ablak", LogWindow.show)
      .menuItem("Debug", "Napló másolása a vágólapra", ClipboardPlus.copyToClipboard(getAllLogs))
      .menuItem("Debug", "Registry adatok törlése", RegistryPlus.clear(IdaLibrary.getClass.getName)) // FIXME!!! remember takes the title of the JFrame! should take name of JFrame!!!!
      .menuItem("Segítség", "Hibajelentés emailben", writeEmail)
      .menuItem("Segítség", "About", showAbout)
      .onCloseDisposeWithCondition(confirmExit)
      .onWindowClosed({ LogWindow.close; WebCam.stop })
      .packAndSetVisible
      .positionToCenter
      .maximize

    controller.initBindings // TODO has to be after get view (set selection model for list)

    val initFocusComponent = controller.getComponentForFocus
    if (initFocusComponent != null) initFocusComponent.requestFocusInWindow
  }
}