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

object DialogHelper {


  /**
   * Blockiert!!!
   *
   * @param parent
   * @param controller
   * @param input
   * @return
   */
  def startModalDialog[INPUT, OUTPUT](parent: Component, controller: IDialogController[INPUT, OUTPUT], input: INPUT): OkCancelDialog = {
    controller.initData(input);

    val parentWindow: Window = if (parent == null) null else SwingUtilities.windowForComponent(parent)

    val dialog = new OkCancelDialog(parentWindow)
    dialog.setLocationRelativeTo(parent)
    dialog.setModal(true)

    val panel = new JPanelWithFrameLayout().withBorders.withSeparators.newColumn("pref:grow")
    panel.add(getButtons(dialog, controller))
    panel.newRow("top:pref:grow").add(controller.getView())

    dialog.add(addScrollableInBorders(panel))
    dialog.setTitle(controller.getTitle)
    dialog.setResizable(true)
    dialog.addWindowListener(new WindowAdapter() { override def windowClosing(e: WindowEvent) = super.windowClosed(e) })//TODO needed?

    controller.initBindings
    dialog.pack
    dialog.positionToCenter
    controller.addResizeListener(dialog)
    dialog.maximize
    
    // blockiert:
    dialog.setVisible(true)
    if (parentWindow != null) parentWindow.invalidate()

    dialog
  }

  def addScrollableInBorders(component: Component): Component = {
    val layout = new FormLayout("3dlu,pref:grow,3dlu", "3dlu,pref:grow,3dlu")
    val panel = new JPanel(layout)
    val COL_ROW_MIDDLE = 2
    panel.add(component, CC.xy(COL_ROW_MIDDLE, COL_ROW_MIDDLE))
    val scrollPane = new JScrollPane(panel)
    scrollPane.setBorder(null)
    scrollPane
  }

  def getButtons(dialog: OkCancelDialog, dialogController: IDialogController[_, _]) = {
    val panel = new JPanelWithFrameLayout().withSeparators.newColumn("pref:grow")

    panel.newColumn
    panel.addButton("Mentés").name("Mentés").onAction({ dialogController.onOk(); dialog.setOk(true); dialog.dispose() })

    panel.newColumn
    panel.addButton("Mégsem").name("Mégsem").onAction({ dialogController.onCancel(); dialog.dispose() })
    panel
  }

  def yes(texts: TextsDialogYes):Option[String] = yes(texts.text, texts.title, texts.yes)
  def yes(text: Text, title: Text, yes: Text) = Alert.alertOptions(title.get, text.get, Array(yes.get))
    
  def yesNo(parent: Component, texts: TextsDialogYesNo):Boolean = yesNo(parent, texts.text, texts.title, texts.yes, texts.no)
  def yesNo(parent: Component, question: Text, dialogTitle: Text, yes: Text, no: Text) =
    Text.combineAndExecute("question" -> question, "title" -> dialogTitle, "yes" -> yes, "no" -> no)(texts =>
      JOptionPane.showOptionDialog(
        parent,
        texts("question"),
        texts("title"),
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        Array[Object](texts("yes"), texts("no")),
        texts("no")) == JOptionPane.OK_OPTION)

  trait  DialogResult
  case object YES extends DialogResult 
  case object NO extends DialogResult
  case object CANCEL extends DialogResult
  
  def yesNoCancel(parent: Component, texts: TextsDialogYesNoCancel):DialogResult = yesNoCancel(parent, texts.text, texts.title, texts.yes, texts.no, texts.cancel)
  def yesNoCancel(parent: Component, text: Text, title: Text, yes: Text, no: Text, cancel: Text):DialogResult =
    Text.combineAndExecute("question" -> text, "title" -> title, "yes" -> yes, "no" -> no, "cancel" -> cancel)(texts => {
      val result = JOptionPane.showOptionDialog(
        parent,
        texts("question"),
        texts("title"),
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        Array[Object](texts("yes"), texts("no"), texts("cancel")),
        texts("cancel"))
      if(result == JOptionPane.OK_OPTION) YES
      else if(result == JOptionPane.NO_OPTION) NO
      else CANCEL
    })
    
    def fileChooser(parent: Component, currentDir:File, fileFilter: String, texts: TextsDialogFileChooser, action: File => Unit) = {
      UIManager.put("FileChooser.cancelButtonText", texts.cancel.get)
      new JFileChooser()
        .withCurrentDirectory(currentDir)
        .withDialogTitle(texts.title.get)
        .withApproveButtonText(texts.approve.get)
        .withFileFilter(fileFilter, texts.fileFilterText.get)
        .showAndHandleResult(parent, action)
  }
}