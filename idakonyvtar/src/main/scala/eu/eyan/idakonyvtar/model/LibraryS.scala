package eu.eyan.idakonyvtar.model

import java.util.ArrayList
import lombok.Getter
import lombok.Setter

class LibraryS {

  @Getter
  @Setter
  var books: java.util.List[Book] = new ArrayList()

  @Getter
  @Setter
  var columns: java.util.List[String] = new ArrayList()

  @Getter
  @Setter
  var configuration: ColumnKonfiguration = new ColumnKonfiguration()
}