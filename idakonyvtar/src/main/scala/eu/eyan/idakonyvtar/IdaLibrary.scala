package eu.eyan.idakonyvtar

import eu.eyan.log.Log
import eu.eyan.util.registry.RegistryGroup
import eu.eyan.util.string.StringPlus.StringPlusImplicit

object IdaLibrary {

  // TODO - menu - Help - about, write email, ötlet, javaslat,
  // TODO - menu - Help - seg<tség a konfigurációhoz
  // TODO - menü gombok több köztes hely
  // TODO - mentse el a legutolsó kiválsztott webcamet
  // TODO - több webcam kiesik bejön -> legyen rendesen kezelve hibaüzenetekkel(?)
	// TODO - új könyv: isbn legyen az alap
	// TODO - új könyv: fentre rendezve minden
	// TODO - új könyv: ha lehet jframe maximalizálva
	// TODO - új könyv: mentés gomb kicsi
  // TODO - új könyv: képek ha nincs, akkor is kép hogy nincs
  // TODO - új könyv: képek címfelirat
  // TODO - új könyv: képek fentre rendezve
  // TODO - új könyv: webcam preview
  // TODO - új könyv: képek ahány kép, annyi férjen ki átméretezésnél képek átméretezése
  // TODO - új könyv: mégsem gomb fölöslegesen széles
  // TODO - új könyv: kép Katt átnevezése
  // TODO - új könyv: kép legördülöö vagy autocomplete, frissítse a képet
  // TODO - új könyv: fent - nem kell a címsorban
	// TODO - új könyv - escape close ha nincs változás
	// TODO - új könyv - escape kérdés ha változás
  // TODO - képek: több ktárba, mert több ezer fájl lehetséges
  // TODO - könyv szerkesztése címsor az összes megjelenített oszlop
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
