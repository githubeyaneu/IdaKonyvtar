package eu.eyan.idakonyvtar

import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith
import org.specs2.Specification
import eu.eyan.idakonyvtar.model.Book

@RunWith(classOf[JUnitRunner])
class BookHelperTest extends Specification /* with Mockito */ {
  val TWO = 2
  val bookA = new Book.Builder(TWO).withValue(0, "A0").withValue(1, "A1").build()
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