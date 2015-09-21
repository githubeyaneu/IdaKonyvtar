package eu.eyan.idakonyvtar.controller.adapter;

import scala.collection.JavaConversions.asScalaBuffer
import com.jgoodies.binding.adapter.AbstractTableAdapter
import com.jgoodies.binding.list.SelectionInList
import eu.eyan.idakonyvtar.model.Book
import eu.eyan.idakonyvtar.model.ColumnKonfiguration
import eu.eyan.idakonyvtar.model.ColumnKonfiguration.ColumnConfigurations
import javax.swing.ListModel

object LibraryListTableModel {
  def apply(listModel: SelectionInList[Book], everyColumn: java.util.List[String], columnConfiguration: ColumnKonfiguration): LibraryListTableModel = {

    def columnToShowFilter(columnIndex: Int) =
      columnConfiguration.isTrue(everyColumn.get(columnIndex), ColumnConfigurations.SHOW_IN_TABLE)

    val columnNamesAndIndexesToShow = everyColumn.toList.zipWithIndex.filter(x => columnToShowFilter(x._2))

    if (columnNamesAndIndexesToShow.size < 1)
      throw new IllegalArgumentException(
        "Legalább 1 columnot meg kell jeleníteni! Columnconfigurationban helyesen kell konfigurálni.");

    new LibraryListTableModel(listModel, columnNamesAndIndexesToShow.unzip._2, columnNamesAndIndexesToShow.unzip._1.toArray)
  }
}

class LibraryListTableModel(listModel: SelectionInList[Book], showingColumnIndices: List[Int], columnNames: Array[String])
    extends AbstractTableAdapter[Book](listModel.asInstanceOf[ListModel[_]]: ListModel[_], columnNames: _*) {

  def getSelectedBook(selectedBookIndex: Int) = listModel.getSelection()

  def getValueAt(rowIndex: Int, columnIndex: Int) = getRow(rowIndex).getValue(showingColumnIndices(columnIndex))
}
