package eu.eyan.idakonyvtar.controller;

import java.awt.Component;

trait IController[INPUT, OUTPUT] {

  def getView: Component

  def getTitle: String

  def initData(input: INPUT)

  def initBindings

  def getOutput: OUTPUT

  def getComponentForFocus: Component
}