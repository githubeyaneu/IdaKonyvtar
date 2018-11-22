package eu.eyan.idakonyvtar.controller;

import java.awt.Component
import java.awt.Image
import java.awt.Window
import java.io.File

import scala.collection.mutable.MutableList

import com.google.common.collect.Lists.newArrayList
import com.google.common.io.Resources

import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.BookField
import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.idakonyvtar.oszk.OszkKereso
import eu.eyan.idakonyvtar.oszk.OszkKeresoException
import eu.eyan.idakonyvtar.text.TechnicalTextsIda.ISBN_LABEL
import eu.eyan.idakonyvtar.text.TechnicalTextsIda.ISBN_TEXT
import eu.eyan.idakonyvtar.text.TechnicalTextsIda.MULTIFIELD_SEPARATOR
import eu.eyan.idakonyvtar.text.TechnicalTextsIda.MULTIFIELS_SEPARATOR_REGEX
import eu.eyan.idakonyvtar.util.WebCam
import eu.eyan.idakonyvtar.view.MultiFieldAutocomplete
import eu.eyan.idakonyvtar.view.MultiFieldJTextField
import eu.eyan.log.Log
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.JLabelPlus.JLabelImplicit
import eu.eyan.util.swing.JPanelWithFrameLayout
import eu.eyan.util.swing.JTextFieldAutocomplete
import eu.eyan.util.swing.JTextFieldPlus.JTextFieldPlusImplicit
import eu.eyan.util.swing.SwingPlus
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JTextField

object BookEditor {
  def listForAutocomplete(bookList: Seq[Book], field: BookField) = bookList
    .map(_.getValue(field)) // get the values of the column
    .filter(_ != null) // only not nulls
    .map(s => if (s.contains(MULTIFIELD_SEPARATOR)) getMultiFieldValues(s) else Array(s)) // get all values if multifield
    .flatten // take the whole list
    .++:(List("")) // empty is always the default option
    .map(_.trim)
    // .distinct //do distinct in ac
    //      .sortWith((s1: String, s2: String) => COLLATOR.compare(s1, s2) < 0) //autocomplete does sorting
    .toList

  def editWithIsbn(
    book:       Book,
    fields:     List[BookField],
    bookList:   List[Book],
    loadedFile: File) = new BookEditor(book, fields, bookList, WITH_ISBN, loadedFile)

  def editBookWithoutIsbn(
    book:       Book,
    fields:     List[BookField],
    bookList:   List[Book],
    loadedFile: File) = new BookEditor(book, fields, bookList, NO_ISBN, loadedFile)
  
  private def getMultiFieldValues(value: String) = value.split(MULTIFIELS_SEPARATOR_REGEX).filter(!_.isEmpty())
  private def encodeMultiFieldValues(values: TraversableOnce[String]) = values.mkString(MULTIFIELD_SEPARATOR)
}

private trait IsbnSetting
private case object NO_ISBN extends IsbnSetting
private case object WITH_ISBN extends IsbnSetting

class BookEditor private (
  private val book:        Book,
  private val fields:      List[BookField],
  private val bookList:    List[Book],
  private val isbnEnabled: IsbnSetting,
  private val loadedFile:  File) {

  def getComponent = view
  def getResult = createResult

  private val TEXTFIELD_DEFAULT_SIZE = 20
  private val editors: MutableList[Component] = MutableList() // FIXME: delete afterwards
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

  case class BookFieldEditor(val component: Component, valueGetter: () => String) {
    def getValue: String = valueGetter()
  }

  val fieldEditors = for { fieldIndex <- 0 until fields.size } yield {
    val field = fields(fieldIndex)
    val value = book.getValue(field)
    Log.debug(s"column $field")
    fieldsPanel.newRow.addLabel(field.fieldName)

    val isMultiEditorField = field.isMulti
    val isAutocompleteField = field.isAutocomplete
    val isPictureField = field.isPicture

    val editor: BookFieldEditor =
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
          textField.setText("") //book.setValue(field)("")
          refreshImage
        })

        textField.setText(value)
        BookFieldEditor(imgNameAndBtn, textField.getText)
      } else {
        if (isAutocompleteField) {
          if (isMultiEditorField) {
            val multiAutocomplete = new MultiFieldAutocomplete(field.fieldName, "Autocomplete", "Nincs találat").setAutoCompleteList(BookEditor.listForAutocomplete(bookList, field))
            multiAutocomplete.setValues(BookEditor.getMultiFieldValues(value))
            BookFieldEditor(multiAutocomplete, () => BookEditor.encodeMultiFieldValues(multiAutocomplete.getValues))
          } else {
            val autocompleteEditor = new JTextFieldAutocomplete().setHintText("Autocomplete").setAutocompleteList(BookEditor.listForAutocomplete(bookList, field))
            autocompleteEditor.setText(value)
            BookFieldEditor(autocompleteEditor, () => autocompleteEditor.getText)
          }
        } else {
          if (isMultiEditorField) {
            val multiEditor = new MultiFieldJTextField(field.fieldName)
            multiEditor.setValues(BookEditor.getMultiFieldValues(value))
            BookFieldEditor(multiEditor, () => BookEditor.encodeMultiFieldValues(multiEditor.getValues))
          } else {
            val textField = new JTextField(TEXTFIELD_DEFAULT_SIZE)
            textField.setText(value)
            BookFieldEditor(textField, () => textField.getText)
          }
        }
      }

    editor.component.setName(field.fieldName)
    fieldsPanel.nextColumn.add(editor.component)
    editors += editor.component
    field -> editor
  }

  private def createResult = {
    def getEditorResult(fieldAndEditor: (BookField, BookFieldEditor)): (BookField, String) = {
      val field = fieldAndEditor._1
      val editor = fieldAndEditor._2
      field -> editor.getValue
    }
    val fieldsAndValues = fieldEditors.toList map getEditorResult
    Book(fieldsAndValues)
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
}