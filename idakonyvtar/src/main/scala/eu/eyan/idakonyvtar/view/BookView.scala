package eu.eyan.idakonyvtar.view;

import java.awt.Component

import scala.collection.mutable.MutableList

import scala.collection.mutable.MutableList
import com.jgoodies.forms.builder.PanelBuilder
import com.jgoodies.forms.factories.CC
import com.jgoodies.forms.layout.FormLayout
import AbstractView.addRow
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

//object BookView {
//
//}
//
//class BookView //extends AbstractView 
//{
//  
//}