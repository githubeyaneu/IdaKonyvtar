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
import eu.eyan.idakonyvtar.view.BookView
import eu.eyan.idakonyvtar.view.MultiField
import eu.eyan.idakonyvtar.view.MultiFieldJComboBox
import eu.eyan.idakonyvtar.view.MultiFieldJTextField
import javax.swing.ImageIcon
import javax.swing.JComboBox
import javax.swing.JOptionPane
import javax.swing.JTextField
import javax.swing.SwingUtilities

class BookController extends IDialogController[BookControllerInput, Book] {
  val view = new BookView()
  var model: BookControllerInput = null
  val resizeListeners: java.util.List[Window] = newArrayList()

  def getView = view.getComponent()

  def getTitle =
    if (model.columns.indexOf("Szerző") >= 0)
      "Book adatainak szerkesztése - " + model.book.getValue(model.columns.indexOf("Szerző"))
    else
      "Book adatainak szerkesztése"

  def initData(model: BookControllerInput) = {
    this.model = model
    view.setColumns(model.columns)
    view.setIsbnEnabled(model.isbnEnabled)
    view.setColumnConfiguration(model.columnConfiguration)
  }

  def initBindings() = {
    initFieldsActionBindings
    view.getIsbnText().addActionListener(isbnSearch())
  }

  private def initFieldsActionBindings = {
    for (columnIndex <- 0 until model.columns.size()) {
      val columnName = model.columns.get(columnIndex)
      val autoComplete = model.columnConfiguration.isTrue(columnName, ColumnConfigurations.AUTOCOMPLETE)
      val multi = model.columnConfiguration.isTrue(columnName, ColumnConfigurations.MULTIFIELD)

      if (autoComplete) {
        val columnList = BookHelper.getColumnList(model.bookList, columnIndex)
        if (multi) {
          val mmcombo: MultiFieldJComboBox = view.getEditors().get(columnIndex).asInstanceOf[MultiFieldJComboBox]
          mmcombo.setAutoCompleteList(columnList)
          multiFieldBind(mmcombo, new BookFieldValueModel(columnIndex, model.book))
        } else {
          val comboBox: JComboBox[_] = view.getEditors().get(columnIndex).asInstanceOf[JComboBox[_]]
          val adapter = new ComboBoxAdapter[String](columnList, new BookFieldValueModel(columnIndex, model.book))
          Bindings.bind(comboBox, adapter)
          AutoCompleteDecorator.decorate(comboBox)
        }
      } else {
        if (multi) {
          val mmc: MultiFieldJTextField = view.getEditors().get(columnIndex).asInstanceOf[MultiFieldJTextField]
          multiFieldBind(mmc, new BookFieldValueModel(columnIndex, model.book))
        } else {
          Bindings.bind(view.getEditors().get(columnIndex).asInstanceOf[JTextField], new BookFieldValueModel(columnIndex, model.book))
        }
      }
    }
  }

  private def multiFieldBind(mmc: MultiField[String, _], bookFieldValueModel: BookFieldValueModel) = {
    bookFieldValueModel.addValueChangeListener(new PropertyChangeListener {
      override def propertyChange(evt: PropertyChangeEvent) =
        if (evt.getNewValue() != evt.getOldValue())
          mmc.setValues(getMultiFieldList(evt.getNewValue().asInstanceOf[String]))
    })

    mmc.setValues(getMultiFieldList(bookFieldValueModel.getValue()))

    mmc.addPropertyChangeListener(new PropertyChangeListener {
      override def propertyChange(evt: PropertyChangeEvent) =
        bookFieldValueModel.setValue(mmc.getValues().filter(_ != null).mkString(BookHelper.LISTA_SEPARATOR))
    })
  }

  private def isbnSearch(): ActionListener = new ActionListener() {
    override def actionPerformed(e: ActionEvent) = {
      if (e.getSource() == view.getIsbnText()) {
        view.getIsbnText().selectAll()
        view.getIsbnSearchLabel().setText("Keresés")
        view.getIsbnSearchLabel().setIcon(new ImageIcon(Resources.getResource("icons/search.gif")))
        view.getEditors().foreach(_.setEnabled(false))

        //TODO Asynchron
        SwingUtilities.invokeLater(new Runnable() {
          override def run() {
            try {
              val marcsToIsbn = OszkKereso.getMarcsToIsbn(view.getIsbnText().getText().replaceAll("ö", "0"))
              prozessIsbnData(marcsToIsbn)
            } catch {
              case e: OszkKeresoException =>
                // FIXME: itt fontos a naplózás
                view.getIsbnSearchLabel().setText("Nincs találat")
                view.getIsbnSearchLabel().setIcon(new ImageIcon(Resources.getResource("icons/error.gif")))
            } finally {
              view.getEditors().foreach(_.setEnabled(true))
              fireResizeEvent()
            }
          }
        })
        fireResizeEvent()
      }
    }
  }

  private def prozessIsbnData(marcsFromOszk: List[Marc]) =
    model.columns.foreach(column => {
      try {
        val marcCodesFromColumns = model.columnConfiguration.getMarcCodes(column)
        val values = for (
          marcFromOszk <- marcsFromOszk;
          marcFromColumn <- marcCodesFromColumns if (isMarcsApply(marcFromOszk, marcFromColumn))
        ) yield marcFromOszk.getValue()
        model.book.setValue(model.columns.indexOf(column), values.mkString(", "))
      } catch {
        case e: Exception =>
          e.printStackTrace()
          JOptionPane.showMessageDialog(null, e.getLocalizedMessage())
      }
    })

  private def isMarcsApply(marcFromOszk: Marc, marcFromColumn: Marc) = {
    if (marcFromOszk == null || marcFromColumn == null || marcFromOszk.getMarc1() == null || marcFromColumn.getMarc1() == null)
      false
    else marcFromOszk.getMarc1().equalsIgnoreCase(marcFromColumn.getMarc1()) &&
      (marcFromColumn.getMarc2().equals("") || marcFromOszk.getMarc2().equalsIgnoreCase(marcFromColumn.getMarc2())) &&
      (marcFromColumn.getMarc3().equals("") || marcFromOszk.getMarc3().equalsIgnoreCase(marcFromColumn.getMarc3()))
  }

  def onOk = {}

  def onCancel = {}

  def getOutput = model.book

  def getComponentForFocus(): Component = view.getIsbnText()

  def addResizeListener(window: Window) = this.resizeListeners.add(window)

  private def fireResizeEvent() = resizeListeners.foreach(_.pack)

  private def getMultiFieldList(value: String): List[String] = value.split(BookHelper.LISTA_SEPARATOR_REGEX).filter(!_.isEmpty()).toList
}