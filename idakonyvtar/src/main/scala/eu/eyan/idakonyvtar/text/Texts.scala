package eu.eyan.idakonyvtar.text

import rx.lang.scala.Observable
import rx.lang.scala.subjects.BehaviorSubject
import eu.eyan.util.scala.Try


// TODO write tests
object Texts {
  val NO = "Nem"
  val TITLE = "IdaKönyvtár"
  val YES = "Igen"
  val NO_BOOK_FOR_THE_FILTER = "Ilyen szűrőfeltételekkel nem található book."
  val NO_BOOK_IN_THE_LIST = "Nincs book a listában."
  val ERROR_AT_READING = "Hiba a beolvasáskor"

}

sealed abstract class TextN(val txt: String, args: Observable[Any]*) extends Observable[String] {
  private val textObservable = BehaviorSubject[String]()

  if (args.nonEmpty) {
    def toText(params: List[Any]) = Try(String.format(txt, params.map(_.asInstanceOf[Object]): _*)).getOrElse("")
    val observablesCombined = Observable.combineLatest(args.toIterable)(_.toList).map(toText)
    observablesCombined.subscribe(textObservable)
  }
  val asJavaObservable: rx.Observable[_ <: String] = textObservable.asJavaObservable
}

case object IdaLibraryTitle0 extends TextN(s"IdaKönyvtár - zzz db Könyv") // TODO: delete this is just an example
case class IdaLibraryTitle(nrOfBooks: Observable[Int]) extends TextN(s"IdaKönyvtár - %s db Könyv", nrOfBooks)
