package eu.eyan.idakonyvtar.controller;

import com.google.common.collect.Lists.newArrayList
import java.awt.Component
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.beans.PropertyChangeEvent
import java.util.List
import java.util.stream.Collectors
import javax.swing.ImageIcon
import javax.swing.JComboBox
import javax.swing.JOptionPane
import javax.swing.JTextField
import javax.swing.SwingUtilities
import org.apache.commons.lang.ObjectUtils
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
import com.google.common.base.Joiner
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
import scala.collection.JavaConversions._
import java.beans.PropertyChangeListener

class BookController extends IDialogController[BookControllerInput, Book] {
  val view = new BookView()
  var model: BookControllerInput = null
  val resizeListeners: java.util.List[Window] = newArrayList()

  def getView = view.getComponent()

  def getTitle =
    if (model.getColumns().indexOf("Szerző") >= 0)
      "Book adatainak szerkesztése - " + model.getBook().getValue(model.getColumns().indexOf("Szerző"))
    else
      "Book adatainak szerkesztése"

  def initData(model: BookControllerInput) = {
    this.model = model
    view.setColumns(model.getColumns())
    view.setIsbnEnabled(model.isIsbnEnabled())
    view.setColumnConfiguration(model.getColumnConfiguration())
  }

  def initBindings() = {
    initFieldsActionBindings
    view.getIsbnText().addActionListener(isbnSearch())
  }

  private def initFieldsActionBindings = {
    for (columnIndex <- 0 until model.getColumns().size()) {
      val columnName = model.getColumns().get(columnIndex)
      val autoComplete = model.getColumnConfiguration().isTrue(columnName, ColumnConfigurations.AUTOCOMPLETE)
      val multi = model.getColumnConfiguration().isTrue(columnName, ColumnConfigurations.MULTIFIELD)
      if (autoComplete) {
        val columnList = BookHelper.getColumnList(model.getBookList(), columnIndex)
        if (multi) {
          val mmcombo: MultiFieldJComboBox = view.getEditors().get(columnIndex).asInstanceOf[MultiFieldJComboBox];
          mmcombo.setAutoCompleteList(columnList);
          multimezőBind(mmcombo, new BookFieldValueModel(columnIndex, model.getBook()));
        } else {
          val comboBox: JComboBox[_] = view.getEditors().get(columnIndex).asInstanceOf[JComboBox[_]]
          val adapter = new ComboBoxAdapter[String](columnList, new BookFieldValueModel(columnIndex, model.getBook()))
          Bindings.bind(comboBox, adapter);
          AutoCompleteDecorator.decorate(comboBox);
        }
      } else {
        if (multi) {
          val mmc: MultiFieldJTextField = view.getEditors().get(columnIndex).asInstanceOf[MultiFieldJTextField]
          multimezőBind(mmc, new BookFieldValueModel(columnIndex, model.getBook()));
        } else {
          Bindings.bind(view.getEditors().get(columnIndex).asInstanceOf[JTextField], new BookFieldValueModel(columnIndex, model.getBook()));
        }
      }
    }
  }

  private def multimezőBind(mmc: MultiField[String, _], bookFieldValueModel: BookFieldValueModel) = {
    bookFieldValueModel.addValueChangeListener(new PropertyChangeListener {
      override def propertyChange(evt: PropertyChangeEvent) = {
        if (ObjectUtils.notEqual(evt.getNewValue(), evt.getOldValue())) {
          mmc.setValues(getMultiFieldList(evt.getNewValue().asInstanceOf[String]));
        }
      }
    })

    mmc.setValues(getMultiFieldList(bookFieldValueModel.getValue().asInstanceOf[String]))

    mmc.addPropertyChangeListener(new PropertyChangeListener {
      override def propertyChange(evt: PropertyChangeEvent) = {
        bookFieldValueModel.setValue(Joiner.on(BookHelper.LISTA_SEPARATOR).skipNulls().join(mmc.getValues()));
      }
    });
  }

  private def isbnSearch(): ActionListener = {
    return new ActionListener() {
      override def actionPerformed(e: ActionEvent) = {
        if (e.getSource() == view.getIsbnText()) {
          System.out.println("ISBN Action: "
            + view.getIsbnText().getText());
          view.getIsbnText().selectAll();
          view.getIsbnSearchLabel().setText("Keresés");
          view.getIsbnSearchLabel().setIcon(
            new ImageIcon(Resources
              .getResource("icons/search.gif")));
          view.getEditors().toList.foreach(_.setEnabled(false))

          //TODO Asynchron
          SwingUtilities.invokeLater(new Runnable() {
            override def run() {
              var marcsToIsbn: List[Marc] = null
              try {
                marcsToIsbn = OszkKereso.getMarcsToIsbn(view
                  .getIsbnText().getText()
                  .replaceAll("ö", "0"));
                prozessIsbnData(marcsToIsbn);
              } catch {
                case e: OszkKeresoException => {
                  // FIXME: itt fontos a naplózás
                  view.getIsbnSearchLabel().setText("Nincs találat");
                  view.getIsbnSearchLabel().setIcon(new ImageIcon(Resources.getResource("icons/error.gif")));
                }
              } finally {
                view.getEditors().toList.foreach(_.setEnabled(true))
                fireResizeEvent();
              }
            }
          });
          fireResizeEvent();
        }
      }
    };
  }

  private def prozessIsbnData(marcsToIsbn: List[Marc]) = {
    model.getColumns().foreach { column =>
      {
        var columnValue = ""
        var marcCodesToColumns: List[Marc] = null
        try {
          marcCodesToColumns = model.getColumnConfiguration().getMarcCodes(column);
          marcsToIsbn.foreach { marc =>
            marcCodesToColumns.foreach { columnMarc =>
              if (isMarcsApply(marc, columnMarc)) {
                if (columnValue.equals("")) columnValue += marc.getValue() else columnValue += ", " + marc.getValue();
              }
            }
          }

          model.getBook().setValue(model.getColumns().indexOf(column), columnValue);
        } catch {
          case e: Exception => {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
          }
        }
      }
    }
  }

  private def isMarcsApply(pontosMarc: Marc, pontatlanMarc: Marc) = {
    var ret: Boolean = false;
    if (pontosMarc == null || pontatlanMarc == null
      || pontosMarc.getMarc1() == null
      || pontatlanMarc.getMarc1() == null) {
      ret = false
    } else if (pontosMarc.getMarc1().equalsIgnoreCase(pontatlanMarc.getMarc1())) {
      if (pontatlanMarc.getMarc2().equals("")
        || pontosMarc.getMarc2().equalsIgnoreCase(
          pontatlanMarc.getMarc2())) {
        if (pontatlanMarc.getMarc3().equals("")
          || pontosMarc.getMarc3().equalsIgnoreCase(
            pontatlanMarc.getMarc3())) {
          ret = true
        }
      }
    }
    ret
  }

  def onOk = {}

  def onCancel = {}

  def getOutput = model.getBook()

  def getComponentForFocus(): Component = view.getIsbnText()

  def addResizeListener(window: Window) = this.resizeListeners.add(window)

  private def fireResizeEvent() = resizeListeners.toList.foreach(_.pack)

  private def getMultiFieldList(value: String): java.util.List[String] = {
    val strings = value.split(BookHelper.LISTA_SEPARATOR_REGEX)
    val list = strings.toList.filter(!_.isEmpty()).toList
    list
  }

}
