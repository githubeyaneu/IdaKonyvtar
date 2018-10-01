package eu.eyan.idakonyvtar

import eu.eyan.util.swing.JTabbedPanePlus
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.io.File
import java.net.URI
import java.net.URLEncoder

import eu.eyan.idakonyvtar.controller.LibraryController
import eu.eyan.idakonyvtar.text.TextsIda
import eu.eyan.idakonyvtar.util.DialogHelper
import eu.eyan.idakonyvtar.util.DialogHelper.NO
import eu.eyan.idakonyvtar.util.DialogHelper.YES
import eu.eyan.idakonyvtar.util.WebCam
import eu.eyan.log.Log
import eu.eyan.log.LogWindow
import eu.eyan.util.awt.clipboard.ClipboardPlus
import eu.eyan.util.io.FilePlus.FilePlusImplicit
import eu.eyan.util.registry.RegistryPlus
import eu.eyan.util.rx.lang.scala.subjects.BehaviorSubjectPlus.BehaviorSubjectImplicit
import eu.eyan.util.string.StringPlus.StringPlusImplicit
import eu.eyan.util.swing.Alert
import eu.eyan.util.swing.JButtonPlus.JButtonImplicit
import eu.eyan.util.swing.JFramePlus
import eu.eyan.util.swing.JFramePlus.JFramePlusImplicit
import eu.eyan.util.swing.JTabbedPanePlus
import eu.eyan.util.swing.JTextFieldPlus.JTextFieldPlusImplicit
import eu.eyan.util.swing.JToolBarPlus.JToolBarImplicit
import eu.eyan.util.text.Text
import eu.eyan.util.text.Text.emptySingularPlural
import javax.swing.JFrame
import javax.swing.JToolBar
import rx.lang.scala.Subscription
import rx.lang.scala.subjects.BehaviorSubject

class IdaLibraryTabs {
  //TODO refactoring tabs to here...
}