package eu.eyan.idakonyvtar.controller;

import java.awt.Component;

trait IController[INPUT, OUTPUT] {

  def getView(): Component

  @deprecated("change to observable")
  def getTitle(): String

  def initData(input: INPUT): Unit

  def initBindings(): Unit

  def getOutput(): OUTPUT

  def getComponentForFocus(): Component
}