package eu.eyan.idakonyvtar.util;

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Frame
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import org.jdesktop.swingx.JXFrame
import com.jgoodies.forms.builder.PanelBuilder
import com.jgoodies.forms.factories.CC
import com.jgoodies.forms.layout.FormLayout
import eu.eyan.idakonyvtar.controller.IController
import eu.eyan.idakonyvtar.controller.IControllerWithMenu
import eu.eyan.idakonyvtar.controller.IDialogController
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.JToolBar
import javax.swing.SwingUtilities
import eu.eyan.util.swing.OkCancelDialog
import eu.eyan.util.swing.OkCancelDialog
import eu.eyan.util.swing.JPanelWithFrameLayout
import javax.swing.JPanel
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit
import eu.eyan.util.swing.JDialogPlus.JdialogPlusImplicit
import eu.eyan.util.text.Text
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.Alert
import javax.swing.UIManager
import javax.swing.JFileChooser
import eu.eyan.util.swing.JFileChooserPlus.JFileChooserImplicit
import java.io.File
import eu.eyan.util.text.TextsDialogYes
import eu.eyan.util.text.TextsDialogYesNo
import eu.eyan.util.text.TextsDialogFileChooser
import eu.eyan.util.text.TextsDialogYesNoCancel
import eu.eyan.util.swing.JDialogPlus.JdialogPlusImplicit
import java.awt.GraphicsEnvironment
import eu.eyan.util.swing.JPanelPlus.JPanelImplicit
import eu.eyan.util.awt.ContainerPlus.ContainerPlusImplicit
import eu.eyan.util.swing.JComponentPlus.JComponentImplicit
import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import eu.eyan.util.swing.WithComponent
import javax.swing.JDialog

object DialogHelper {

  private class JDialogWithYesNoButtons(owner: Component, content: Component, yesText: Text, noText: Text) extends JDialog(owner.windowForComponent) {
    def hasYesSelected = clickOnYes

    private var clickOnYes = false
    private val yesNoButtonsPanel = new JPanelWithFrameLayout().withSeparators
    yesNoButtonsPanel.newColumn.addButton(yesText).name(SAVE_BUTTON_NAME).onAction { clickOnYes = true; this.dispose }
    yesNoButtonsPanel.newColumn.addButton(noText).name(CANCEL_BUTTON_NAME).onAction(this.dispose)

    private val buttonsAndContent = new JPanelWithFrameLayout().withSeparators.withBorders
      .newColumnScrollable
      .newRow.addFluent(yesNoButtonsPanel)
      .newRowScrollable.addFluentInScrollPane(content)

    add(buttonsAndContent)
  }

  /** Blocks as a modal dialog!!! */
  def yesNoEditor(owner: Component, content: Component, title: Text, yesText: Text, noText: Text) =
    new JDialogWithYesNoButtons(owner, content, yesText, noText)
      .locationRelativeTo(owner)
      .modal
      .title(title)
      .resizeable
      .packFluent
      .positionToCenter
      .maximize
      .visible // blocks !!! for jdialog
      .hasYesSelected

  def yes(texts: TextsDialogYes): Option[String] = yes(texts.text, texts.title, texts.yes)
  def yes(text: Text, title: Text, yes: Text) = Alert.alertOptions(title.get, text.get, Array(yes.get))

  def yesNo(parent: Component, texts: TextsDialogYesNo): Boolean = yesNo(parent, texts.text, texts.title, texts.yes, texts.no)
  def yesNo(parent: Component, question: Text, dialogTitle: Text, yes: Text, no: Text) =
    Text.combineMoreTextsWithNamesAndExecute("question" -> question, "title" -> dialogTitle, "yes" -> yes, "no" -> no)(texts =>
      JOptionPane.showOptionDialog(
        parent,
        texts("question"),
        texts("title"),
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        Array[Object](texts("yes"), texts("no")),
        texts("no")) == JOptionPane.OK_OPTION)

  trait DialogResult
  case object YES extends DialogResult
  case object NO extends DialogResult
  case object CANCEL extends DialogResult

  def yesNoCancel(parent: Component, texts: TextsDialogYesNoCancel): DialogResult = yesNoCancel(parent, texts.text, texts.title, texts.yes, texts.no, texts.cancel)
  def yesNoCancel(parent: Component, text: Text, title: Text, yes: Text, no: Text, cancel: Text): DialogResult =
    Text.combineMoreTextsWithNamesAndExecute("question" -> text, "title" -> title, "yes" -> yes, "no" -> no, "cancel" -> cancel)(texts => {
      val result = JOptionPane.showOptionDialog(
        parent,
        texts("question"),
        texts("title"),
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        Array[Object](texts("yes"), texts("no"), texts("cancel")),
        texts("cancel"))
      if (result == JOptionPane.OK_OPTION) YES
      else if (result == JOptionPane.NO_OPTION) NO
      else CANCEL
    })

  def fileChooser(parent: Component, currentDir: File, fileFilter: String, texts: TextsDialogFileChooser, action: File => Unit) = {
    UIManager.put("FileChooser.cancelButtonText", texts.cancel.get)
    new JFileChooser()
      .withCurrentDirectory(currentDir)
      .withDialogTitle(texts.title.get)
      .withApproveButtonText(texts.approve.get)
      .withFileFilter(fileFilter, texts.fileFilterText.get)
      .showAndHandleResult(parent, action)
  }
}