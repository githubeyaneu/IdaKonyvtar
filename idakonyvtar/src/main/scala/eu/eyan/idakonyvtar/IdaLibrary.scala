package eu.eyan.idakonyvtar

import eu.eyan.log.Log
import eu.eyan.util.registry.RegistryGroup
import eu.eyan.util.string.StringPlus.StringPlusImplicit

object IdaLibrary {

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


  protected val registryGroup = RegistryGroup(classOf[IdaLibrary].getName)
  def registryValue(parameterName: String) = registryGroup.registryValue(parameterName)
  
  // TODO: how to handle it? maven etc...
  def VERSION = "1.1.2"
  
  def main(args: Array[String]): Unit = {
    val fileToOpen = args.lift(0).getOrElse("library.xls").asFileOrResource.get

    Log.activateDebugLevel
    Log.info("Resource -> File: " + fileToOpen)

    IdaLibraryFrame(fileToOpen)
  }
}

class IdaLibrary
