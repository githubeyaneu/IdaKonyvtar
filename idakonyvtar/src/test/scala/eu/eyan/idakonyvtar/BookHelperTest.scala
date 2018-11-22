package eu.eyan.idakonyvtar

import scala.util.Random

import org.junit.Test
import org.junit.runner.RunWith

import eu.eyan.idakonyvtar.controller.BookEditor
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.util.random.RandomPlus
import eu.eyan.testutil.ScalaEclipseJunitRunner
import eu.eyan.idakonyvtar.model.BookField
import eu.eyan.idakonyvtar.text.TechnicalTextsIda
import eu.eyan.util.excel.ExcelColumn

@RunWith(classOf[ScalaEclipseJunitRunner])
class BookHelperTest {
  val TWO = 2
  val bookA = new BookBuilder(TWO).withValue(0, "A0").withValue(1, "A1").build()

  @Test
  def speedColumnList = {
    def books(r: Int) =
      new RandomPlus(r)
        .nextReadableStrings(10000, 10, 20)
        .map { x => Book(List((BookField(ExcelColumn(0), "", List(), Array()), x))) }
        .toList

    def books2(r: Int) = new RandomPlus(r)
      .nextReadableStrings(2000, 10, 20)
      .sliding(2, 2)
      .map { x => Book(List((BookField(ExcelColumn(0), "", List(), Array()), x(0) + TechnicalTextsIda.MULTIFIELD_SEPARATOR + x(1)))) }
      .toList

    def allBooks(r: Int) = (books(r).++(books2(r))).toList
    def shuffledBooks(r: Int) = Random.shuffle(allBooks(r)).toList
    var sum = 0L
    for { i <- 1 to 10 } {
      val b = shuffledBooks(i)
      val start = System.currentTimeMillis
      BookEditor.listForAutocomplete(b, BookField(ExcelColumn(0), "", List(), Array()))
      val end = System.currentTimeMillis
      sum = sum + end - start
    }
    println(sum + "ms all")
  }
}
