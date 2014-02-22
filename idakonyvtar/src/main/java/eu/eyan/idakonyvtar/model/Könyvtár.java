package eu.eyan.idakonyvtar.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class Könyvtár
{
    @Getter
    @Setter
    private List<Könyv> könyvek = newArrayList();

    @Getter
    @Setter
    private List<String> oszlopok = newArrayList();

    @Getter
    @Setter
    private OszlopKonfiguráció konfiguráció = new OszlopKonfiguráció();
}
