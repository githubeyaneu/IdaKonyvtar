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

import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.BookFieldValueModel
import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.idakonyvtar.oszk.OszkKereso
import eu.eyan.idakonyvtar.oszk.OszkKeresoException
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
import eu.eyan.idakonyvtar.model.BookField
import eu.eyan.idakonyvtar.util.ExcelHandler.FieldConfiguration

object BookController {
  def listForAutocomplete(bookList: Seq[Book], field: BookField) = bookList 
    .map(_.getValue(field)) // get the values of the column
    .filter(_ != null) // only not nulls
    .map(s => if (s.contains(MULTIFIELD_SEPARATOR)) s.split(MULTIFIELS_SEPARATOR_REGEX) else Array(s)) // get all values if multifield
    .flatten // take the whole list
    .++:(List("")) // empty is always the default option
    .map(_.trim)
    // .distinct //do distinct in ac
    //      .sortWith((s1: String, s2: String) => COLLATOR.compare(s1, s2) < 0) //autocomplete does sorting
    .toList
}

trait IsbnSetting
case object NO_ISBN extends IsbnSetting 
case object WITH_ISBN extends IsbnSetting
  
  
class BookController(
  private val book:                Book,
  private val fields:              List[BookField], 
  private val bookList:            List[Book],
  private val isbnEnabled:         IsbnSetting, //TODO make two applies for editing and for newBook
  private val loadedFile:          File) {

  def getComponent = view

  private val TEXTFIELD_DEFAULT_SIZE = 20
  private val editors: MutableList[Component] = MutableList()
  private val isbnSearchLabel = new JLabel()
  private val isbnText = new JTextField()
  private val webcamPanel = new JPanelWithFrameLayout
  private val resizeListeners: java.util.List[Window] = newArrayList()

  val fieldsPanel = new JPanelWithFrameLayout().withSeparators.newColumn.newColumn("pref:grow")

  val picturePanel = new JPanelWithFrameLayout().withSeparators.newColumn("320px")

  val view = new JPanelWithFrameLayout().withSeparators
    .newColumn("f:p:g ").addFluent(fieldsPanel)
    .newColumn("f:320px").addFluent(picturePanel)
    .newColumn("f:320px").addFluent(webcamPanel)

  startWebcam

  if (isbnEnabled == WITH_ISBN) {
    fieldsPanel.newRow.span.addSeparatorWithTitle("Isbn")
    fieldsPanel.newRow
    fieldsPanel.add(isbnSearchLabel.name(ISBN_LABEL))
    fieldsPanel.nextColumn.add(isbnText.name(ISBN_TEXT))

    isbnText onHierarchyChanged isbnText.requestFocusInWindow
    isbnText onActionPerformed isbnSearch
  }

  fieldsPanel.newRow.span.addSeparatorWithTitle("Adatok")

  for { fieldIndex <- 0 until fields.size } {
    val field = fields(fieldIndex)
    Log.debug(s"column $field")
    fieldsPanel.newRow.addLabel(field.fieldName)

    val isMultiEditorField = field.isMulti
    val isAutocompleteField = field.isAutocomplete
    val isPictureField = field.isPicture

    val editor: Component =
      if (isPictureField) {
        //TODO WTF spagetti:
        picturePanel.newRow.addLabel(field.fieldName)
        val imageLabel = picturePanel.newRow.addLabel("").name("look" + field)

        val imgNameAndBtn = JPanelWithFrameLayout()
        val textField = imgNameAndBtn.newColumn.addTextField("", TEXTFIELD_DEFAULT_SIZE).name("picturePath")
        val button = imgNameAndBtn.newColumn.addButton("katt").name("click")

        def refreshImage = imageLabel.setIcon(new ImageIcon(book.getImage(field).get.getScaledInstance(320, 240, Image.SCALE_DEFAULT)))
        if (book.getImage(field).nonEmpty) refreshImage
        button.onClicked({
          book.setImage(field)(WebCam.getImage)
          book.setValue(field)("")
          refreshImage
        })

        bindTextField(textField, new BookFieldValueModel(field, book))
        imgNameAndBtn
      } else if (isAutocompleteField)
        if (isMultiEditorField) multiFieldBind(new MultiFieldAutocomplete(field.fieldName, "Autocomplete", "Nincs találat").setAutoCompleteList(BookController.listForAutocomplete(bookList, field)), new BookFieldValueModel(field, book))
        else bindTextField(new JTextFieldAutocomplete().setHintText("Autocomplete").setAutocompleteList(BookController.listForAutocomplete(bookList, field)), new BookFieldValueModel(field, book))
      else if (isMultiEditorField) multiFieldBind(new MultiFieldJTextField(field.fieldName), new BookFieldValueModel(field, book))
      else bindTextField(new JTextField(TEXTFIELD_DEFAULT_SIZE), new BookFieldValueModel(field, book))

    editor.setName(field.fieldName)
    fieldsPanel.nextColumn.add(editor)
    editors += editor
  }

  private def bindTextField(tf: JTextField, bookFieldValueModel: BookFieldValueModel) = {
    Bindings.bind(tf, bookFieldValueModel)
    tf
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
    mmc
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
    fields.foreach(field => {
      try {
        val marcCodesFromColumns = field.marcCodes
        val values = for {
          marcFromOszk <- marcsFromOszk
          marcFromColumn <- marcCodesFromColumns if (isMarcsApply(marcFromOszk, marcFromColumn))
        } yield marcFromOszk.value
        Log.info("BookController.prozessIsbnData " + values.mkString("\r\n    "))
        book.setValue(field)(values.mkString(", "))
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