package eu.eyan.idakonyvtar

<<<<<<< HEAD
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith
import org.specs2.Specification
import eu.eyan.idakonyvtar.model.Book

@RunWith(classOf[JUnitRunner])
class BookHelperTest extends Specification /* with Mockito */ {
  val bookA = new Book.Builder(2).withValue(0, "A0").withValue(1, "A1").build()
  def is = s2"""

 BookHelper getColumnList
   does not return null values            $noNullInReturn
   where example 2 must be true           $e2
                                          """

  def noNullInReturn = 1 must_== 1
  def e1 = 1 must_== 1
  def e2 = 2 must_== 2
}
// Specification {
//  "this is my specification" >> {
//    "where example 1 must be true" >> {
//      1 must_== 1
//    }
//    "where example 2 must be true" >> {
//      2 must_== 2
//    }
//  }
// }
=======
import scala.collection.mutable.MutableList
import scala.util.Random

import org.junit.Test
import org.junit.runner.RunWith

import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.util.BookHelper
import eu.eyan.util.random.RandomPlus
import eu.eyan.testutil.ScalaEclipseJunitRunner

@RunWith(classOf[ScalaEclipseJunitRunner])
class BookHelperTest {
  val TWO = 2
  val bookA = new Book.Builder(TWO).withValue(0, "A0").withValue(1, "A1").build()

  @Test
  def speedColumnList = {
    def books(r: Int) =
      new RandomPlus(r)
        .nextReadableStrings(10000, 10, 20)
        .map { x => new Book(MutableList(x)) }
        .toList

    def books2(r: Int) = new RandomPlus(r)
      .nextReadableStrings(2000, 10, 20)
      .sliding(2, 2)
      .map { x => new Book(MutableList(x(0) + BookHelper.LISTA_SEPARATOR + x(1))) }
      .toList

    def allBooks(r: Int) = (books(r).++(books2(r))).toList
    def shuffledBooks(r: Int) = Random.shuffle(allBooks(r)).toList
    var sum = 0L
    for {i <- 1 to 10} {
      val b = shuffledBooks(i)
      val start = System.currentTimeMillis
      BookHelper.getColumnList(b, 0)
      val end = System.currentTimeMillis
      sum = sum + end - start
    }
    println(sum + "ms all")
  }
}
>>>>>>> branch 'master' of https://github.com/githubeyaneu/IdaKonyvtar.git
