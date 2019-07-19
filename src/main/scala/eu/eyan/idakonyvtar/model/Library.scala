package eu.eyan.idakonyvtar.model

import com.google.common.collect.Lists
import java.io.File
import eu.eyan.util.swing.TableCol
import scala.collection.mutable.MutableList
import scala.collection.mutable.ListBuffer
import eu.eyan.idakonyvtar.util.LibraryExcelHandler.FieldConfiguration

class Library(
  val file:            File,
  private val columns: List[BookField],
  private val books:   ListBuffer[Book]   = ListBuffer[Book]()) {

  def addBook(book: Book) = books += book
  def booksSize = books.size
  def bookAtIndex(index: Int) = books(index)
  def booksAsJavaList = new ListBufferAsJava(books)

  def createEmptyBook = Book.empty(columns)
  def fieldsToRemember = columns.filter(_.isRemember)
  def fieldToName(fieldName: String) = columns.filter(_.fieldName == fieldName).lift(0)
  def getColumns = columns // needed for saving the config

  val columnNamesAndIndexesToShow = columns.zipWithIndex.filter(_._1.isShowInTable)
  val columnNamesToShow = columnNamesAndIndexesToShow.unzip._1
  val columnIndicesToShow = columnNamesAndIndexesToShow.unzip._2 // TODO move to Library...???
  
  
  val columnNamesAndFieldsToShow = columns.zipWithIndex.filter(_._1.isShowInTable)
  val columnFieldsToShow = columnNamesAndFieldsToShow.unzip._1 // TODO move to Library...???
}

class ListBufferAsJava[T](list: ListBuffer[T]) extends java.util.List[T] {
  def size(): Int = list.size
  def get(index: Int): T = list(index)
  def iterator(): java.util.Iterator[T] = new java.util.Iterator[T] {
    val iterator = list.iterator
    def hasNext(): Boolean = iterator.hasNext
    def next(): T = iterator.next
  }
  def set(idx: Int, item: T): T = { list(idx) = item; item }
  def add(idx: Int, item: T): Unit = list.insert(idx, item)

  def add(item: T): Boolean = { list.append(item); true }
  def addAll(idx: Int, items: java.util.Collection[_ <: T]): Boolean = { list.insert(idx, items.toArray.map(_.asInstanceOf[T]): _*); true }
  def addAll(items: java.util.Collection[_ <: T]): Boolean = { list.append(items.toArray.map(_.asInstanceOf[T]): _*); true }
  def clear(): Unit = list.clear
  def contains(elem: Any): Boolean = list.contains(elem)
  def indexOf(elem: Any): Int = list.indexOf(elem)
  def isEmpty(): Boolean = list.isEmpty
  def lastIndexOf(elem: Any): Int = list.lastIndexOf(elem)
  def remove(idx: Int): T = list.remove(idx)

  def containsAll(x$1: java.util.Collection[_]): Boolean = ???
  def listIterator(x$1: Int): java.util.ListIterator[T] = ???
  def listIterator(): java.util.ListIterator[T] = ???
  def remove(x$1: Any): Boolean = ???
  def removeAll(x$1: java.util.Collection[_]): Boolean = ???
  def retainAll(x$1: java.util.Collection[_]): Boolean = ???
  def subList(x$1: Int, x$2: Int): java.util.List[T] = ???
  def toArray[T](x$1: Array[T with Object]): Array[T with Object] = ???
  def toArray(): Array[Object] = ???
}