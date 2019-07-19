package eu.eyan.idakonyvtar.oszk

import org.junit.Test
import org.fest.assertions.Assertions.assertThat
import org.junit.runner.RunWith
import eu.eyan.testutil.ScalaEclipseJunitRunner
import org.junit.Rule
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit
import org.fest.assertions.Fail
import java.io.IOException

@RunWith(classOf[ScalaEclipseJunitRunner])
class OszkKeresoTest {
  val ABIGEL_ISBN = "9789631193701"

  val TIMEOUT = 15

  @Rule
  def globalTimeout = new Timeout(TIMEOUT, TimeUnit.SECONDS);

  @Test def isbnKeresOszkban_muxik() = {
    try {
      assertThat(OszkKereso.isbnKeresOszkban(ABIGEL_ISBN)).contains("Abigél");
    } catch {
      case e: IOException => { e.printStackTrace(); Fail.fail(e.getMessage()) }
    }
  }

  @Test def marc_parse_muxik() = {
    val abigel = OszkKereso.getMarcsToIsbn(ABIGEL_ISBN);
    assertThat(findMarc(abigel, CIM)).isEqualTo("Abigél");

    val marai = OszkKereso.getMarcsToIsbn("9789632273822");
    assertThat(findMarc(marai, CIM)).isEqualTo("Hallgatni akartam");
  }

  def findMarc(marcs: List[Marc], marcCode: MarcCodes) = {
    marcs.filter(marc =>
      marcCode.marc1.equals(marc.marc1)
        && marcCode.marc2.equals(marc.marc2)
        && marcCode.marc3.equals(marc.marc3)).headOption.getOrElse(null).value
  }

  class MarcCodes(val marc1: String, val marc2: String, val marc3: String)
  case object CIM extends MarcCodes("245", "10", "a")
}