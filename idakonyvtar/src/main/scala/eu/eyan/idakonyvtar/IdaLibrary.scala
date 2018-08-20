package eu.eyan.idakonyvtar

import java.io.File

import com.google.common.io.Resources

import eu.eyan.idakonyvtar.controller.LibraryController
import eu.eyan.idakonyvtar.controller.input.LibraryControllerInput
import eu.eyan.idakonyvtar.text.Texts
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

    val fileToOpen =
      if (file.exists()) file
      else new File(Resources.getResource(path).getFile)

    Log.activateInfoLevel
    Log.info("Resource -> File: " + fileToOpen)

    val controller = new LibraryController
    val jMenuBar = controller.getMenuBar
    val toolBar = controller.getToolBar

    controller.initData(new LibraryControllerInput(fileToOpen))
    val frame = new JFrame()
      .onCloseExit
      .title(controller.getTitle)
      .name(Texts.TITLE)
      .jMenuBar(jMenuBar)
      .addFluent(toolBar, BorderLayout.NORTH)
      .addFluent(controller.getView)
      .packAndSetVisible
      .positionToCenter
      .maximize
      .onWindowClosing({ LogWindow.close; WebCam.stop })
      
    controller.initBindings

    val initFocusComponent = controller.getComponentForFocus()
    if (initFocusComponent != null) initFocusComponent.requestFocusInWindow()
  }
}