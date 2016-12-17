package eu.eyan.idakonyvtar

import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith
import org.specs2.Specification
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.testutil.ScalaEclipseJunitRunner
import org.junit.Test
import org.fest.assertions.Assertions._
import eu.eyan.idakonyvtar.util.BookHelper
import eu.eyan.util.random.RandomPlus
import scala.collection.mutable.MutableList
import scala.util.Random

@RunWith(classOf[ScalaEclipseJunitRunner])
class BookHelperTest {
			  
  @Test
  def speedColumnList = {
    def books(r: Int) = new RandomPlus(r).nextReadableStrings(10000, 10, 20).map { x => new Book(MutableList(x)) }.toList
    def books2(r: Int) = new RandomPlus(r).nextReadableStrings(2000, 10, 20).sliding(2, 2).map { x => new Book(MutableList(x(0) + BookHelper.LISTA_SEPARATOR + x(1))) }.toList
    def allBooks(r: Int) = (books(r).++(books2(r))).toList
    def shuffledBooks(r: Int) = Random.shuffle(allBooks(r)).toList
    var sum = 0L
    for (i <- 1 to 10) {
      val b = shuffledBooks(i)
      val start = System.currentTimeMillis
      BookHelper.getColumnList(b, 0)
      val end = System.currentTimeMillis
      sum = sum + end - start
    }
    println(sum + "ms all")
  }
}
