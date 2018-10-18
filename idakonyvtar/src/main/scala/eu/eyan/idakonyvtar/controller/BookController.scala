package eu.eyan.idakonyvtar.controller;

import java.awt.Component
import java.awt.Image
import java.awt.Window
import java.awt.event.ActionListener
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.File

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.mutable.MutableList

import com.google.common.collect.Lists.newArrayList
import com.google.common.io.Resources
import com.jgoodies.binding.adapter.Bindings
import com.jgoodies.forms.layout.FormLayout
import com.jgoodies.forms.layout.RowSpec

import eu.eyan.idakonyvtar.controller.input.BookControllerInput
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.BookFieldValueModel
import eu.eyan.idakonyvtar.model.ColumnConfigurations
import eu.eyan.idakonyvtar.model.ColumnKonfiguration
import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.idakonyvtar.oszk.OszkKereso
import eu.eyan.idakonyvtar.oszk.OszkKeresoException
import eu.eyan.idakonyvtar.util.BookHelper
import eu.eyan.idakonyvtar.util.LibraryException
import eu.eyan.idakonyvtar.util.WebCam
import eu.eyan.idakonyvtar.view.MultiField
import eu.eyan.idakonyvtar.view.MultiFieldAutocomplete
import eu.eyan.idakonyvtar.view.MultiFieldJTextField
import eu.eyan.log.Log
import eu.eyan.util.awt.AwtHelper
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.JLabelPlus.JLabelImplicit
import eu.eyan.util.swing.JPanelWithFrameLayout
import eu.eyan.util.swing.JTextFieldAutocomplete
import eu.eyan.util.swing.SwingPlus
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextField
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import eu.eyan.util.swing.WithComponent
import eu.eyan.util.swing.JTextFieldPlus.JTextFieldPlusImplicit

object BookController {
  def getColumnList(bookList: Seq[Book], columnIndex: Int) = bookList
    .map(_.getValue(columnIndex)) // get the values of the column
    .filter(_ != null) // only not nulls
    .map(s => if (s.contains(MULTIFIELD_SEPARATOR)) s.split(MULTIFIELS_SEPARATOR_REGEX) else Array(s)) // get all values if multifield
    .flatten // take the whole list
    .++:(List("")) // empty is always the default option
    .map(_.trim)
    // .distinct //do distinct in ac
    //      .sortWith((s1: String, s2: String) => COLLATOR.compare(s1, s2) < 0) //autocomplete does sorting
    .toList
}
class BookController(
  private val book:                Book,
  private val columns:             List[String],
  private val columnConfiguration: ColumnKonfiguration,
  private val bookList:            List[Book],
  private val isbnEnabled:         Boolean             = false,
  private val loadedFile:          File) {

  def getComponent = view

  private val TEXTFIELD_DEFAULT_SIZE = 20
  private val editors: MutableList[Component] = MutableList()
  private val images: MutableList[JLabel] = MutableList()
  private val isbnSearchLabel = new JLabel()
  private val isbnText = new JTextField()
  private val webcamPanel = new JPanelWithFrameLayout
  private val view = createViewComponent
  private val resizeListeners: java.util.List[Window] = newArrayList()

  
  private def createViewComponent(): Component = {
    val rowsPanel = new JPanelWithFrameLayout().withSeparators
    rowsPanel.newColumn.newColumn("pref:grow")
    val imagesPanel = new JPanelWithFrameLayout().name("imagesPanel").withSeparators.newColumn("320px")
    startWebcam

    if (isbnEnabled) {
      rowsPanel.newRow.span.addSeparatorWithTitle("Isbn")
      rowsPanel.newRow
      rowsPanel.add(isbnSearchLabel.name(ISBN_LABEL))
      rowsPanel.nextColumn.add(isbnText.name(ISBN_TEXT))

      isbnText onHierarchyChanged isbnText.requestFocusInWindow
      isbnText onActionPerformed isbnSearch
    }

    rowsPanel.newRow.span.addSeparatorWithTitle("Adatok")

    for { columnIndex <- 0 until columns.size } {
      val columnName = columns(columnIndex)
      Log.debug(s"column $columnName")
      rowsPanel.newRow.addLabel(columnName)

      val isMultiEditorField = columnConfiguration.isTrue(columnName, ColumnConfigurations.MULTIFIELD)
      val isAutocompleteField = columnConfiguration.isTrue(columnName, ColumnConfigurations.AUTOCOMPLETE)
      val isPictureField = columnConfiguration.isTrue(columnName, ColumnConfigurations.PICTURE)

      val editor: Component =
        if (isPictureField) {
          //TODO WTF spagetti:
          val panel = JPanelWithFrameLayout()
          imagesPanel.newRow.addLabel(columnName)
          val imageLabel = imagesPanel.newRow.addLabel("").name("look" + columnName)
          images += imageLabel
          panel.newColumn.addTextField("", TEXTFIELD_DEFAULT_SIZE).name("picturePath")
          panel.newColumn.addButton("katt").name("click")

          if (book.values(columnIndex) != "") {
            val dir = loadedFile.getParentFile
            val imagesDir = (loadedFile.getAbsolutePath + ".images").asDir
            val imageFile = (imagesDir.getAbsolutePath + "\\" + book.values(columnIndex)).asFile
            val image = ImageIO.read(imageFile)
            book.images.put(columnIndex, image)
          }

          val text = panel.getComponents.filter(_.getName == "picturePath")(0).asInstanceOf[JTextField]
          val button = panel.getComponents.filter(_.getName == "click")(0).asInstanceOf[JButton]
          val look = images.filter(_.getName == "look" + columnName)(0)
          if (book.images.contains(columnIndex)) look.setIcon(new ImageIcon(book.images(columnIndex).getScaledInstance(320, 240, Image.SCALE_DEFAULT)))
          Bindings.bind(text, new BookFieldValueModel(columnIndex, book))
          button.onClicked({
            book.images.put(columnIndex, WebCam.getImage)
            book.values(columnIndex) = ""
            look.setIcon(new ImageIcon(book.images(columnIndex).getScaledInstance(320, 240, Image.SCALE_DEFAULT)))
          })

          panel
        } else if (isAutocompleteField) {
          val columnList = BookController.getColumnList(bookList, columnIndex)
          if (isMultiEditorField) {
            val mfac = new MultiFieldAutocomplete(columnName, "Autocomplete", "Nincs találat")
            Log.debug("mac " + columnIndex + " " + columnList)
            mfac.setAutoCompleteList(columnList)
            multiFieldBind(mfac, new BookFieldValueModel(columnIndex, book))
            mfac
          } else {
            val ac = new JTextFieldAutocomplete().setHintText("Autocomplete")
            Log.debug("ac " + columnIndex + " " + columnList)
            ac.setAutocompleteList(columnList)
            Bindings.bind(ac, new BookFieldValueModel(columnIndex, book))
            ac
          }
        } else {
          if (isMultiEditorField) {
            val mtf = new MultiFieldJTextField(columnName)
            multiFieldBind(mtf, new BookFieldValueModel(columnIndex, book))
            mtf
          } else {
            val tf = new JTextField(TEXTFIELD_DEFAULT_SIZE)
            Bindings.bind(tf, new BookFieldValueModel(columnIndex, book))
            tf
          }
        }

      editor.setName(columnName)
      rowsPanel.nextColumn.add(editor)
      editors += editor
    }

    val panel = new JPanelWithFrameLayout().withSeparators
    panel.newColumn("f:p:g ").add(rowsPanel)
    panel.newColumn("f:320px").add(imagesPanel)
    panel.newColumn("f:320px").add(webcamPanel)


    panel

  }

  private def multiFieldBind(mmc: MultiField[String, _], bookFieldValueModel: BookFieldValueModel) = {
    Log.debug(mmc.getName + " " + bookFieldValueModel)
    bookFieldValueModel.addValueChangeListener(new PropertyChangeListener {
      override def propertyChange(evt: PropertyChangeEvent) =
        if (evt.getNewValue() != evt.getOldValue()) {
          Log.debug("Listener: " + evt.getOldValue + " -> " + evt.getNewValue)
          mmc.setValues(getMultiFieldList(evt.getNewValue().asInstanceOf[String]))
        }
    })

    mmc.setValues(getMultiFieldList(bookFieldValueModel.getValue()))

    mmc.addPropertyChangeListener(new PropertyChangeListener {
      override def propertyChange(evt: PropertyChangeEvent) =
        bookFieldValueModel.setValue(mmc.getValues().filter(_ != null).mkString(MULTIFIELD_SEPARATOR))
    })
  }

  private def isbnSearch() = {
    isbnText.selectAll()
    isbnSearchLabel.setText("Keresés")
    isbnSearchLabel.setIcon(new ImageIcon(Resources.getResource("icons/search.gif")))
    editors.foreach(_.setEnabled(false))

    SwingPlus.invokeLater {
      {
        try {
          val marcsToIsbn = OszkKereso.getMarcsToIsbn(isbnText.getText().replaceAll("ö", "0"))
          prozessIsbnData(marcsToIsbn)
        } catch {
          case e: OszkKeresoException =>
            Log.error(e)
            isbnSearchLabel.setText("Nincs találat")
            isbnSearchLabel.setIcon(new ImageIcon(Resources.getResource("icons/error.gif")))
        } finally {
          editors.foreach(_.setEnabled(true))
        }
      }
    }
  }

  private def prozessIsbnData(marcsFromOszk: List[Marc]) =
    columns.foreach(column => {
      try {
        val marcCodesFromColumns = columnConfiguration.getMarcCodes(column)
        val values = for {
          marcFromOszk <- marcsFromOszk
          marcFromColumn <- marcCodesFromColumns if (isMarcsApply(marcFromOszk, marcFromColumn))
        } yield marcFromOszk.value
        Log.info("BookController.prozessIsbnData " + values.mkString("\r\n    "))
        book.setValue(columns.indexOf(column))(values.mkString(", "))
      } catch {
        case e: Exception =>
          e.printStackTrace()
          JOptionPane.showMessageDialog(null, e.getLocalizedMessage())
      }
    })

  private def isMarcsApply(marcFromOszk: Marc, marcFromColumn: Marc) = {
    // Log.debug("marcFromOszk: " + marcFromOszk + " =?= marcFromColumn:" + marcFromColumn)
    if (marcFromOszk == null || marcFromColumn == null || marcFromOszk.marc1 == null || marcFromColumn.marc1 == null)
      false
    else marcFromOszk.marc1.equalsIgnoreCase(marcFromColumn.marc1) &&
      (marcFromColumn.marc2.equals("") || marcFromOszk.marc2.equalsIgnoreCase(marcFromColumn.marc2)) &&
      (marcFromColumn.marc3.equals("") || marcFromOszk.marc3.equalsIgnoreCase(marcFromColumn.marc3))
  }

  private def startWebcam = {
    Log.info("start webcam")
    val webcam = WebCam.startWebcam
    if (webcam.nonEmpty) {
      webcamPanel.newRow.add(webcam.get.picker)
      webcamPanel.newRow.add(webcam.get.panel)
    } else webcamPanel.add(new JLabel("No Webcam"))
  }

  private def getMultiFieldList(value: String): List[String] = value.split(MULTIFIELS_SEPARATOR_REGEX).filter(!_.isEmpty()).toList
}