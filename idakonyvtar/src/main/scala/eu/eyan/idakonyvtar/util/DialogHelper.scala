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

object DialogHelper {

  val CANCEL = "Mégsem"
  val SAVE = "Mentés"

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
    dialog.addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent) = super.windowClosed(e)
    })

    controller.initBindings
    dialog.pack
    positionToCenter(dialog)
    controller.addResizeListener(dialog)
    dialog.setSize(1200, 768)
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

  def runInFrame[INPUT](
    parent: Component,
    controller: IController[INPUT, _],
    input: INPUT,
    jMenuBar: JMenuBar,
    toolBar: JToolBar,
    fullScreen: Boolean,
    name: String) =
    {
      controller.initData(input)
      val frame = new JXFrame()
      frame.add(toolBar, BorderLayout.NORTH)
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      frame.add(controller.getView())
      frame.setTitle(controller.getTitle())
      frame.setName(name)
      frame.setJMenuBar(jMenuBar)
      controller.initBindings()
      frame.setVisible(true)
      frame.pack()
      positionToCenter(frame)
      if (fullScreen) frame.setExtendedState(Frame.MAXIMIZED_BOTH)
      val initFocusComponent = controller.getComponentForFocus()
      if (initFocusComponent != null) initFocusComponent.requestFocusInWindow()
      frame
    }

  def positionToCenter(component: Component) = {
    val screenSize = Toolkit.getDefaultToolkit().getScreenSize()
    val width = component.getSize().width
    val height = component.getSize().height
    component.setSize(width, height)
    component.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2)
  }

  def getButtons(dialog: OkCancelDialog, dialogController: IDialogController[_, _]) = {
    val panel = new JPanelWithFrameLayout().withSeparators.newColumn("pref:grow")
    
    panel.newColumn
    panel.addButton(SAVE).name(SAVE).onAction( {dialogController.onOk(); dialog.setOk(true); dialog.dispose()})
    
    panel.newColumn
    panel.addButton(CANCEL).name(CANCEL).onAction( {dialogController.onCancel(); dialog.dispose()})
    panel
  }

  def runInFrameFullScreen[INPUT](controller: IControllerWithMenu[INPUT, _], input: INPUT, name: String): JFrame =
    runInFrame(null, controller, input, controller.getMenuBar(), controller.getToolBar(), true, name)

  def yesNo(parent: Component, question: String, dialogTitle: String) =
    JOptionPane.showOptionDialog(parent,
      question,
      dialogTitle,
      JOptionPane.YES_NO_OPTION,
      JOptionPane.QUESTION_MESSAGE,
      null,
      Array[Object]("Igen", CANCEL),
      CANCEL) == JOptionPane.OK_OPTION
}