package eu.eyan.idakonyvtar.oszk;

class Marc(marc1: String, marc2: String, marc3: String, value: String) {

  override def toString() = "Marc [marc1=" + marc1 + ", marc2=" + marc2 + ", marc3=" + marc3 + ", value=" + value + "]"

  def getMarc1() = marc1
  def getMarc2() = marc2
  def getMarc3() = marc3
  def getValue() = value
}