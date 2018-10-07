package eu.eyan.idakonyvtar.controller;

import java.awt.Component
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.util.List
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.seqAsJavaList
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
import com.google.common.collect.Lists.newArrayList
import com.google.common.io.Resources
import com.jgoodies.binding.adapter.Bindings
import com.jgoodies.binding.adapter.ComboBoxAdapter
import eu.eyan.idakonyvtar.controller.input.BookControllerInput
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.BookFieldValueModel
import eu.eyan.idakonyvtar.model.ColumnConfigurations
import eu.eyan.idakonyvtar.oszk.Marc
import eu.eyan.idakonyvtar.oszk.OszkKereso
import eu.eyan.idakonyvtar.oszk.OszkKeresoException
import eu.eyan.idakonyvtar.util.BookHelper
import eu.eyan.idakonyvtar.util.LibraryException
import eu.eyan.idakonyvtar.view.MultiField
import eu.eyan.idakonyvtar.view.MultiFieldAutocomplete
import eu.eyan.idakonyvtar.view.MultiFieldJTextField
import eu.eyan.log.Log
import eu.eyan.util.swing.JTextFieldAutocomplete
import javax.swing.ImageIcon
import javax.swing.JOptionPane
import javax.swing.JTextField
import javax.swing.SwingUtilities
import eu.eyan.log.Log
import eu.eyan.idakonyvtar.view.MultiFieldAutocomplete
import eu.eyan.util.swing.JTextFieldAutocomplete
import eu.eyan.util.awt.AwtHelper
import eu.eyan.util.swing.SwingPlus
import javax.swing.JPanel
import javax.swing.JButton
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import java.awt.Image
import eu.eyan.idakonyvtar.util.WebCam
import javax.swing.JLabel
import eu.eyan.util.swing.JLabelPlus.JLabelImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import javax.imageio.ImageIO
import java.lang.Thread.UncaughtExceptionHandler
import com.github.sarxos.webcam.WebcamPicker
import com.github.sarxos.webcam.WebcamPanel
import com.github.sarxos.webcam.WebcamResolution
import com.github.sarxos.webcam.WebcamResolution
import scala.collection.mutable.MutableList
import eu.eyan.idakonyvtar.model.ColumnKonfiguration
import eu.eyan.util.swing.JPanelWithFrameLayout
import scala.collection.mutable.MutableList
import com.jgoodies.forms.builder.PanelBuilder
import com.jgoodies.forms.factories.CC
import com.jgoodies.forms.layout.FormLayout
import eu.eyan.idakonyvtar.model.ColumnConfigurations
import eu.eyan.idakonyvtar.model.ColumnKonfiguration
import eu.eyan.util.swing.JTextFieldAutocomplete
import javax.swing.JLabel
import javax.swing.JTextField
import eu.eyan.util.swing.JPanelWithFrameLayout
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit
import eu.eyan.log.Log
import com.github.sarxos.webcam.WebcamPanel
import com.github.sarxos.webcam.WebcamPicker
import com.github.sarxos.webcam.WebcamResolution
import java.lang.Thread.UncaughtExceptionHandler
import javax.swing.JPanel
import com.jgoodies.forms.layout.RowSpec

class BookController extends IDialogController[BookControllerInput, Book] {
  val SPACE = " "
    val ISBN_TEXT = "isbnText";
  val ISBN_LABEL = "isbnLabel";
  
  def addRow(layout: FormLayout, spec: String): Int = {
    spec.split(",").foreach(row => layout.appendRow(RowSpec.decode(row)))
    spec.split(",").length
  }



  val TEXTFIELD_DEFAULT_SIZE = 20
  val SEPARATOR_PREF = "3dlu, pref"

  val editors: MutableList[Component] = MutableList()
  val images: MutableList[JLabel] = MutableList()
  val isbnSearchLabel = new JLabel()
  val isbnText = new JTextField()

  var columns: List[String] = null
  def setColumns(columns: List[String]) = this.columns = columns

  var isbnEnabled: Boolean = false
  def setIsbnEnabled(isbnEnabled: Boolean): Unit = this.isbnEnabled = isbnEnabled

  var columnConfiguration: ColumnKonfiguration = null
  def setColumnConfiguration(columnConfiguration: ColumnKonfiguration) = this.columnConfiguration = columnConfiguration

  var webcamPanel = new JPanelWithFrameLayout 


  
  private  def createViewComponent(): Component = {
    val rowsPanel = new JPanelWithFrameLayout().withSeparators
    rowsPanel.newColumn.newColumn("pref:grow")
    val imagesPanel = new JPanelWithFrameLayout().name("imagesPanel").withSeparators.newColumn("320px")

    

    if (isbnEnabled) {
      rowsPanel.newRow.span.addSeparatorWithTitle("Isbn")
      rowsPanel.newRow
      rowsPanel.add(isbnSearchLabel.name(ISBN_LABEL))
      rowsPanel.nextColumn.add(isbnText.name(ISBN_TEXT))
    }

    rowsPanel.newRow.span.addSeparatorWithTitle("Adatok")

    for { i <- 0 until columns.size } {
      val columnName = columns(i)
      Log.debug(s"column $columnName")
      rowsPanel.newRow.addLabel(columnName)

      val isMultiEditorField = columnConfiguration.isTrue(columnName, ColumnConfigurations.MULTIFIELD)
      val isAutocompleteField = columnConfiguration.isTrue(columnName, ColumnConfigurations.AUTOCOMPLETE)
      val isPictureField = columnConfiguration.isTrue(columnName, ColumnConfigurations.PICTURE)

      val editor: Component =
        if (isPictureField) {
          val panel = JPanelWithFrameLayout()
          imagesPanel.newRow.addLabel(columnName)
          val imageLabel = imagesPanel.newRow.addLabel("").name("look" + columnName)
          images += imageLabel
          panel.newColumn.addTextField("", TEXTFIELD_DEFAULT_SIZE).name("picturePath")
          panel.newColumn.addButton("katt").name("click")
          panel
        } else if (isAutocompleteField) {
          if (isMultiEditorField) new MultiFieldAutocomplete(columnName, "Autocomplete", "Nincs találat")
          else new JTextFieldAutocomplete().setHintText("Autocomplete")
        } else {
          if (isMultiEditorField) new MultiFieldJTextField(columnName)
          else new JTextField(TEXTFIELD_DEFAULT_SIZE)
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
  
  
  val view = createViewComponent
  
  var model: BookControllerInput = null
  val resizeListeners: java.util.List[Window] = newArrayList()

  def getView = view

  def getTitle =
    if (model.columns.indexOf("Cím") >= 0)
      "Könyv adatainak szerkesztése - " + model.book.getValue(model.columns.indexOf("Cím"))
    else
      "Könyv adatainak szerkesztése"

  def initData(model: BookControllerInput) = {
    this.model = model
    setColumns(model.columns)
    setIsbnEnabled(model.isbnEnabled)
    setColumnConfiguration(model.columnConfiguration)
  }

  def initBindings() = {
    initFieldsActionBindings
    isbnText.addActionListener(isbnSearch())
  }

  private def initFieldsActionBindings = {
    for { columnIndex <- 0 until model.columns.size() } {
      val columnName = model.columns.get(columnIndex)
      val autoComplete = model.columnConfiguration.isTrue(columnName, ColumnConfigurations.AUTOCOMPLETE)
      val multi = model.columnConfiguration.isTrue(columnName, ColumnConfigurations.MULTIFIELD)
      val picture = model.columnConfiguration.isTrue(columnName, ColumnConfigurations.PICTURE)

      if (picture) {
        if (model.book.values(columnIndex) != "") {
          val dir = model.loadedFile.getParentFile
          val imagesDir = (model.loadedFile.getAbsolutePath + ".images").asDir
          val imageFile = (imagesDir.getAbsolutePath + "\\" + model.book.values(columnIndex)).asFile
          val image = ImageIO.read(imageFile)
          model.book.images.put(columnIndex, image)
        }

        val panel = editors(columnIndex).asInstanceOf[JPanel]
        val text = panel.getComponents.filter(_.getName == "picturePath")(0).asInstanceOf[JTextField]
        val button = panel.getComponents.filter(_.getName == "click")(0).asInstanceOf[JButton]
        val look = images.filter(_.getName == "look" + columnName)(0)
        //val look = panel.getComponents.filter(_.getName == "look")(0).asInstanceOf[JLabel]
        //look.onMouseClicked(if (model.book.images.contains(columnIndex)) look.setIcon(new ImageIcon(model.book.images(columnIndex))))
        if (model.book.images.contains(columnIndex)) look.setIcon(new ImageIcon(model.book.images(columnIndex).getScaledInstance(320, 240, Image.SCALE_DEFAULT)))
        Bindings.bind(text, new BookFieldValueModel(columnIndex, model.book))
        button.onClicked({
          model.book.images.put(columnIndex, WebCam.getImage)
          model.book.values(columnIndex) = ""
          look.setIcon(new ImageIcon(model.book.images(columnIndex).getScaledInstance(320, 240, Image.SCALE_DEFAULT)))
        })

      } else if (autoComplete) {
        val columnList = BookHelper.getColumnList(model.bookList, columnIndex)
        if (multi) {
          Log.debug("mac " + columnIndex + SPACE + columnList)
          //          val mmcombo: MultiFieldJComboBox = view.editors(columnIndex).asInstanceOf[MultiFieldJComboBox]
          //          mmcombo.setAutoCompleteList(columnList)
          val mmcombo = editors(columnIndex).asInstanceOf[MultiFieldAutocomplete]
          mmcombo.setAutoCompleteList(columnList)
          multiFieldBind(mmcombo, new BookFieldValueModel(columnIndex, model.book))
        } else {
          Log.debug("ac " + columnIndex + SPACE + columnList)
          //        val comboBox: JComboBox[_] = view.editors(columnIndex).asInstanceOf[JComboBox[_]]
          //        val adapter = new ComboBoxAdapter[String](columnList, new BookFieldValueModel(columnIndex, model.book))
          //        Bindings.bind(comboBox, adapter)
          //        AutoCompleteDecorator.decorate(comboBox)
          val autocomplete = editors(columnIndex).asInstanceOf[JTextFieldAutocomplete]
          autocomplete.setAutocompleteList(columnList)
          Bindings.bind(autocomplete, new BookFieldValueModel(columnIndex, model.book))
        }
      } else {
        if (multi) {
          val mmc: MultiFieldJTextField = editors(columnIndex).asInstanceOf[MultiFieldJTextField]
          multiFieldBind(mmc, new BookFieldValueModel(columnIndex, model.book))
        } else {
          Bindings.bind(editors(columnIndex).asInstanceOf[JTextField], new BookFieldValueModel(columnIndex, model.book))
        }
      }
    }

    startWebcam
  }

  private def multiFieldBind(mmc: MultiField[String, _], bookFieldValueModel: BookFieldValueModel) = {
    Log.debug(mmc.getName + SPACE + bookFieldValueModel)
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
        bookFieldValueModel.setValue(mmc.getValues().filter(_ != null).mkString(BookHelper.LISTA_SEPARATOR))
    })
  }

  private def isbnSearch(): ActionListener = AwtHelper.onActionPerformed { e =>
    {
      if (e.getSource() == isbnText) {
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
              fireResizeEvent
            }
          }
        }
        fireResizeEvent
      }
    }
  }

  private def prozessIsbnData(marcsFromOszk: List[Marc]) =
    model.columns.foreach(column => {
      try {
        val marcCodesFromColumns = model.columnConfiguration.getMarcCodes(column)
        val values = for {
          marcFromOszk <- marcsFromOszk
          marcFromColumn <- marcCodesFromColumns if (isMarcsApply(marcFromOszk, marcFromColumn))
        } yield marcFromOszk.value
        Log.info("BookController.prozessIsbnData " + values.mkString("\r\n    "))
        model.book.setValue(model.columns.indexOf(column), values.mkString(", "))
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



  def startWebcam = {
	  Log.info("start webcam")
    val webcam = WebCam.startWebcam
    if (webcam.nonEmpty) {
    	webcamPanel.newRow.add(webcam.get.picker)
      webcamPanel.newRow.add(webcam.get.panel)
    }
    else webcamPanel.add(new JLabel("No Webcam"))
  }
  def stopWebcam = { }

  def onOk = {stopWebcam}

  def onCancel = {stopWebcam}

  def getOutput = model.book

  def getComponentForFocus(): Component = isbnText

  def addResizeListener(window: Window) = this.resizeListeners.add(window)

  private def fireResizeEvent() = resizeListeners.foreach(_.pack)

  private def getMultiFieldList(value: String): List[String] = value.split(BookHelper.LISTA_SEPARATOR_REGEX).filter(!_.isEmpty()).toList
}