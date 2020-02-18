package eu.eyan.idakonyvtar.controller

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File

import com.google.common.io.Resources
import eu.eyan.idakonyvtar.model.{Book, BookField}
import eu.eyan.idakonyvtar.oszk.{Marc, OszkKereso, OszkKeresoException}
import eu.eyan.idakonyvtar.text.TechnicalTextsIda.{ISBN_LABEL, ISBN_TEXT, MULTIFIELD_SEPARATOR, MULTIFIELS_SEPARATOR_REGEX}
import eu.eyan.idakonyvtar.util.WebCam
import eu.eyan.log.Log
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.JComponentPlus.JComponentImplicit
import eu.eyan.util.swing.JLabelPlus.JLabelImplicit
import eu.eyan.util.swing.JTextFieldPlus.JTextFieldPlusImplicit
import eu.eyan.util.swing._
import javax.swing._

object BookEditor {
  def listForAutocomplete(bookList: Seq[Book], field: BookField) = bookList
    .map(_.getValue(field)) // get the values of the column
    .filter(_ != null) // only not nulls
    .flatMap(s => if (s.contains(MULTIFIELD_SEPARATOR)) getMultiFieldValues(s) else List(s)) // get all values if multifield
    // take the whole list
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

  private def getMultiFieldValues(value: String) = value.split(MULTIFIELS_SEPARATOR_REGEX).filter(!_.isEmpty).toList
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
  private val isbnSearchLabel = new JLabel()
  private val isbnText = new JTextField()
  private val webcamPanel = new JPanelWithFrameLayout

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

  case class BookFieldEditor(field: BookField, component: JComponent, valueGetter: () => String, valueSetter: String => Unit, imageGetter: () => BufferedImage = () => null) {
    def getValue = valueGetter()
		def getImage = imageGetter()
    def setValue(value: String) = valueSetter(value)
    def enable = component.enabled
    def disable = component.disabled
  }

  val fieldEditors = for { fieldIndex <- fields.indices } yield {
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

        var picture: BufferedImage = null
        def setImage(image: BufferedImage) = {
          picture = image
          imageLabel.setIcon(new ImageIcon(image.getScaledInstance(320, 240, Image.SCALE_DEFAULT)))
        }
        book.getImage(field).foreach(setImage)
        button.onClicked({
          textField.setText("")
          val image = WebCam.getImage
          setImage(image)
        })

        textField.setText(value)
        BookFieldEditor(field, imgNameAndBtn, textField.getText, textField.setText, () => picture)
      } else {
        if (isAutocompleteField) {
          if (isMultiEditorField) {
            val multiAutocomplete = new MultiFieldAutocomplete(field.fieldName, "Autocomplete", "Nincs találat", BookEditor.listForAutocomplete(bookList, field))
            multiAutocomplete.setValues(BookEditor.getMultiFieldValues(value))
            BookFieldEditor(field, multiAutocomplete, () => BookEditor.encodeMultiFieldValues(multiAutocomplete.getValues), value => multiAutocomplete.setValues(List(value)))
          } else {
            val autocompleteEditor = new JTextFieldAutocomplete().setHintText("Autocomplete").setAutocompleteList(BookEditor.listForAutocomplete(bookList, field))
            autocompleteEditor.setText(value)
            BookFieldEditor(field, autocompleteEditor, autocompleteEditor.getText, autocompleteEditor.setText)
          }
        } else {
          if (isMultiEditorField) {
            val multiEditor = new MultiFieldJTextField(field.fieldName)
            multiEditor.setValues(BookEditor.getMultiFieldValues(value))
            BookFieldEditor(field, multiEditor, () => BookEditor.encodeMultiFieldValues(multiEditor.getValues), value => multiEditor.setValues(List(value)))
          } else {
            val textField = new JTextField(TEXTFIELD_DEFAULT_SIZE)
            textField.setText(value)
            BookFieldEditor(field, textField, textField.getText, textField.setText)
          }
        }
      }

    editor.component.setName(field.fieldName)
    fieldsPanel.nextColumn.add(editor.component)
    editor
  }

  private def createResult = {
    def getEditorResult(fieldAndEditor: BookFieldEditor): (BookField, String) = fieldAndEditor.field -> fieldAndEditor.getValue
		def getEditorImage(fieldAndEditor: BookFieldEditor): (BookField, BufferedImage) = fieldAndEditor.field -> fieldAndEditor.getImage
    val fieldsAndValues = fieldEditors.toList map getEditorResult
    val fieldsAndPictures = fieldEditors.toList map getEditorImage
    Book(fieldsAndValues).withPictures(fieldsAndPictures.toMap.filter(_._2!=null))
  }

  private def isbnSearch() = {
    isbnText.selectAll
    isbnSearchLabel.setText("Keresés") // TODO
    isbnSearchLabel.setIcon(new ImageIcon(Resources.getResource("icons/search.gif"))) //TODO
    fieldEditors.foreach(_.disable)

    SwingPlus.invokeLater {
      try {
        val marcsToIsbn = OszkKereso.getMarcsToIsbn(isbnText.getText.replaceAll("ö", "0"))
        prozessIsbnData(marcsToIsbn)
      } catch {
        case e: OszkKeresoException =>
          Log.error(e)
          isbnSearchLabel.setText("Nincs találat") // TODO
          isbnSearchLabel.setIcon(new ImageIcon(Resources.getResource("icons/error.gif"))) // TODO
      } finally {
        fieldEditors.foreach(_.enable)
      }
    }
  }

  private def prozessIsbnData(marcsFromOszk: List[Marc]) =
    fieldEditors.foreach(fieldEditor => {
      try {
        val fieldMarcCodes = fieldEditor.field.marcCodes
        val values = for {
          marcFromOszk <- marcsFromOszk
          fieldMarcCode <- fieldMarcCodes
          if isMarcsApply(marcFromOszk, fieldMarcCode)
        } yield marcFromOszk.value
        Log.info("BookController.prozessIsbnData " + values.mkString("\r\n    "))
        fieldEditor.setValue(values.mkString(", "))
      } catch {
        case e: Exception =>
          e.printStackTrace()
          JOptionPane.showMessageDialog(null, e.getLocalizedMessage) // TODO
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