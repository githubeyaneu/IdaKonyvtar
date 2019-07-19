package eu.eyan.idakonyvtar.util

import java.awt.Component
import java.io.File

import eu.eyan.idakonyvtar.text.TechnicalTextsIda._
import eu.eyan.util.awt.ComponentPlus.ComponentPlusImplicit
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.JDialogPlus.JdialogPlusImplicit
import eu.eyan.util.swing.JFileChooserPlus.JFileChooserImplicit
import eu.eyan.util.swing.{Alert, JPanelWithFrameLayout}
import eu.eyan.util.text._
import javax.swing.{JDialog, JFileChooser, JOptionPane, UIManager}

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