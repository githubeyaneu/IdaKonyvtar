package eu.eyan.idakonyvtar

import eu.eyan.idakonyvtar.IdaLibraryTestNoLanguage.library
import eu.eyan.idakonyvtar.testhelper.{AbstractUiTest, IdaLibraryTestHelper}
import eu.eyan.idakonyvtar.text.TextsIda
import eu.eyan.testutil.video.VideoRunner
import eu.eyan.util.java.lang.ThreadPlus
import eu.eyan.util.registry.RegistryPlus
import eu.eyan.util.scala.Try
import org.fest.swing.core.matcher.JButtonMatcher
import org.junit.{After, Before, Test}

object IdaLibraryTestNoLanguage {
  private val library: IdaLibraryTestHelper = new IdaLibraryTestHelper()

  def main(args: Array[String]) = {
    val t = new IdaLibraryTestNoLanguage()
    t.setUp
  }
}

class IdaLibraryTestNoLanguage extends AbstractUiTest {

  @Before
  def setUp = {
    Try(RegistryPlus.clear(classOf[IdaLibrary].getName))
    ThreadPlus.run(IdaLibrary.main(Array("library.xls")))
    VideoRunner.setFullScreenToRecord
  }

  @After
  def tearDown = library.cleanUp

  @Test
  def start_NoLanguage_None = {
    RegistryPlus.readOption(classOf[IdaLibrary].getName, classOf[TextsIda].getName) ==> None
    val langDialog = library.dialog
    library.requireNotExists
    langDialog.close
    val frame = waitFor(library.frame)
    RegistryPlus.readOption(classOf[IdaLibrary].getName, classOf[TextsIda].getName) ==> None
    frame.target.getTitle ==> "IdaLibrary - 2 books"
  }

  @Test
  def start_NoLanguage_Magyar = {
    RegistryPlus.readOption(classOf[IdaLibrary].getName, classOf[TextsIda].getName) ==> None
    val langDialog = library.dialog
    library.requireNotExists
    langDialog.component.getTitle ==> "Language selection"
    langDialog.optionPane.requireMessage("Please select your language!")
    langDialog.button(JButtonMatcher.withText("Magyar")).requireText("Magyar") // :)
    langDialog.button(JButtonMatcher.withText("English")).requireText("English") // :)
    langDialog.button(JButtonMatcher.withText("Deutsch")).requireText("Deutsch") // :)
    langDialog.button(JButtonMatcher.withText("Magyar")).click
    val frame = waitFor(library.frame)
    RegistryPlus.readOption(classOf[IdaLibrary].getName, classOf[TextsIda].getName).get ==> "Magyar"
    frame.target.getTitle ==> "IdaKönyvtár - 2 könyv"
  }
}