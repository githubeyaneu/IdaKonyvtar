package eu.eyan.idakonyvtar.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class Library
{
    @Getter
    @Setter
    private List<Book> books = newArrayList();

    @Getter
    @Setter
    private List<String> columns = newArrayList();

    @Getter
    @Setter
    private ColumnKonfiguration configuration = new ColumnKonfiguration();
}
