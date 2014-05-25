package eu.eyan.idakonyvtar.controller.input;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import eu.eyan.idakonyvtar.model.Book;
import eu.eyan.idakonyvtar.model.ColumnKonfiguration;

public class BookControllerInput
{

    public final static boolean ISBN_ENABLED = true;

    @Getter
    @Setter
    private Book book;

    @Getter
    @Setter
    private List<String> columns;

    @Getter
    private boolean isbnEnabled = false;

    @Getter
    private ColumnKonfiguration columnConfiguration = null;

    @Getter
    private List<Book> bookList;

    private BookControllerInput()
    {
    }

    public static class Builder
    {
        private BookControllerInput input = new BookControllerInput();

        public Builder withBook(Book book)
        {
            this.input.book = book;
            return this;
        }

        public Builder withBookList(List<Book> bookList)
        {
            this.input.bookList = bookList;
            return this;
        }

        public Builder withColumns(List<String> columnok)
        {
            this.input.columns = columnok;
            return this;
        }

        public Builder withIsbnEnabled(boolean isbnEnabled)
        {
            this.input.isbnEnabled = isbnEnabled;
            return this;
        }

        public Builder withColumnConfiguration(ColumnKonfiguration columnConfiguration)
        {
            this.input.columnConfiguration = columnConfiguration;
            return this;
        }

        public BookControllerInput build()
        {
            if (this.input.book == null)
            {
                throw new RuntimeException("A könyv nem lehet null");
            }
            if (this.input.bookList == null)
            {
                throw new RuntimeException("A könyvlista nem lehet null");
            }
            if (this.input.columns == null)
            {
                throw new RuntimeException("Az oszlop nem lehet null");
            }
            if (this.input.columnConfiguration == null)
            {
                throw new RuntimeException("Az oszlopkonfiguráció nem lehet null");
            }
            return this.input;
        }

    }
}
