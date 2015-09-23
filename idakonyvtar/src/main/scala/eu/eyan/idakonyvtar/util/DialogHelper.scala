package eu.eyan.idakonyvtar.util;

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.JToolBar
import javax.swing.SwingUtilities
import org.jdesktop.swingx.JXFrame
import com.jgoodies.forms.builder.PanelBuilder
import com.jgoodies.forms.factories.CC
import com.jgoodies.forms.layout.FormLayout
import eu.eyan.idakonyvtar.controller.IController
import eu.eyan.idakonyvtar.controller.IControllerWithMenu
import eu.eyan.idakonyvtar.controller.IDialogController;
import java.awt.Frame

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
    val panelBuilder = new PanelBuilder(new FormLayout("pref:grow", "top:pref:grow, 3dlu, pref"))
    panelBuilder.add(controller.getView(), CC.xy(1, 1))
    panelBuilder.add(getButtons(dialog, controller), CC.xy(1, 3))
    dialog.add(addScrollableInBorders(panelBuilder.build()));
    dialog.setTitle(controller.getTitle());
    dialog.setResizable(true);
    dialog.addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent) = super.windowClosed(e)
    })

    controller.initBindings();
    dialog.pack();
    positionToCenter(dialog);
    controller.addResizeListener(dialog);

    // blockiert:
    dialog.setVisible(true);
    if (parentWindow != null) {
      parentWindow.invalidate();
    }
    return dialog;
  }

  def addScrollableInBorders(component: Component): Component =
    {
      val panelBuilder = new PanelBuilder(new FormLayout("3dlu,pref:grow,3dlu", "3dlu,pref:grow,3dlu"))
      panelBuilder.add(component, CC.xy(2, 2))
      val scrollPane = new JScrollPane(panelBuilder.build())
      scrollPane.setBorder(null)
      return scrollPane
    }

  def runInFrame[INPUT](parent: Component, controller: IController[INPUT, _], input: INPUT, jMenuBar: JMenuBar, toolBar: JToolBar, fullScreen: Boolean, name: String): JFrame =
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

  def positionToCenter(component: Component) =
    {
      val screenSize = Toolkit.getDefaultToolkit().getScreenSize()
      val width = component.getSize().width
      val height = component.getSize().height
      component.setSize(width, height)
      component.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2)
    }

  def getButtons(dialog: OkCancelDialog, dialogController: IDialogController[_, _]): Component =
    {
      val panelBuilder = new PanelBuilder(new FormLayout("pref:grow,3dlu,pref,3dlu,pref", "pref"))
      val okButton = new JButton(SAVE)
      okButton.setName(SAVE)
      val cancelButton = new JButton(CANCEL)
      cancelButton.setName(CANCEL);
      panelBuilder.add(okButton, CC.xy(3, 1));
      panelBuilder.add(cancelButton, CC.xy(5, 1));
      okButton.addActionListener(new ActionListener() {
        override def actionPerformed(e: ActionEvent) =
          {
            dialogController.onOk()
            dialog.setOk(true)
            dialog.dispose()
          }
      })
      cancelButton.addActionListener(new ActionListener() {
        override def actionPerformed(e: ActionEvent) =
          {
            dialogController.onCancel()
            dialog.dispose()
          }
      })
      return panelBuilder.build()
    }

  def runInFrameFullScreen[INPUT](controller: IControllerWithMenu[INPUT, _], input: INPUT, name: String): JFrame =
    return runInFrame(null, controller, input, controller.getMenuBar(), controller.getToolBar(), true, name)

  def yesNo(parent: Component, question: String, dialogTitle: String): Boolean =
    JOptionPane.showOptionDialog(parent,
      question,
      dialogTitle,
      JOptionPane.YES_NO_OPTION,
      JOptionPane.QUESTION_MESSAGE,
      null,
      Array[Object]("Igen", "Mégsem"),
      "Mégsem") == JOptionPane.OK_OPTION
}
