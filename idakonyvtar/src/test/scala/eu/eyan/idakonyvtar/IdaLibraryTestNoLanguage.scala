package eu.eyan.idakonyvtar

import java.io.File

import org.fest.swing.core.matcher.JButtonMatcher
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import IdaLibraryTestNoLanguage.library
import eu.eyan.idakonyvtar.model.ColumnConfigurations
import eu.eyan.idakonyvtar.testhelper.IdaLibraryTestHelper
import eu.eyan.idakonyvtar.testhelper.LibraryFileBuilder
import eu.eyan.idakonyvtar.text.TextsIda
import eu.eyan.idakonyvtar.util.ExcelHandler
import eu.eyan.idakonyvtar.view.LibraryMenuAndToolBar
import eu.eyan.testutil.ExcelAssert
import eu.eyan.testutil.video.VideoRunner
import eu.eyan.util.swing.HighlightRenderer
import org.fest.swing.fixture.FrameFixture
import eu.eyan.log.LogWindow
import eu.eyan.util.awt.clipboard.ClipboardPlus
import eu.eyan.log.Log
import eu.eyan.util.registry.RegistryPlus
import eu.eyan.util.scala.Try
import org.fest.swing.core.ComponentMatcher
import java.awt.Component
import org.fest.swing.fixture.DialogFixture
import eu.eyan.util.java.lang.RunnablePlus
import eu.eyan.util.java.lang.ThreadPlus

object IdaLibraryTestNoLanguage {

  private var library: IdaLibraryTestHelper = new IdaLibraryTestHelper()

  def main(args: Array[String]) = {
    val t = new IdaLibraryTestNoLanguage()
    t.setUp
  }

}

class IdaLibraryTestNoLanguage extends AbstractUiTest {

  @Before
  def setUp = {
    Try( RegistryPlus.clear(classOf[IdaLibrary].getName) )
    ThreadPlus.run(library.start)
    VideoRunner.setFullScreenToRecord
  }

  @After
  def tearDown = library.cleanUp

  @Test
  def startNoLanguage = {
    library.requireNotExists
    val langDialog = waitFor(new DialogFixture(library.robot, "dialog0"))
    langDialog.component.getTitle ==> "Language selection"
    langDialog.optionPane.requireMessage("Please select your language!")
    langDialog.button(JButtonMatcher.withText("Magyar")).requireText("Magyar") // :)
    langDialog.button(JButtonMatcher.withText("English")).requireText("English") // :)
    langDialog.button(JButtonMatcher.withText("Deutsch")).requireText("Deutsch") // :)
    langDialog.button(JButtonMatcher.withText("Magyar")).click
    waitFor(library.requireVisible)
  }
}