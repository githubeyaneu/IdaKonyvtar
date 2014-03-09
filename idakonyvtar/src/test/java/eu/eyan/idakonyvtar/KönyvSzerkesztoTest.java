package eu.eyan.idakonyvtar;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eyan.idakonyvtar.model.OszlopKonfiguráció.OszlopKonfigurációk.AUTOCOMPLETE;
import static eu.eyan.idakonyvtar.model.OszlopKonfiguráció.OszlopKonfigurációk.MULTIMEZŐ;
import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import javax.swing.SwingUtilities;

import org.fest.swing.core.BasicRobot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eyan.idakonyvtar.controller.KönyvController;
import eu.eyan.idakonyvtar.controller.input.KönyvControllerInput;
import eu.eyan.idakonyvtar.model.Könyv;
import eu.eyan.idakonyvtar.model.OszlopKonfiguráció;
import eu.eyan.idakonyvtar.testhelper.KönyvszerkesztőTestHelper;
import eu.eyan.idakonyvtar.util.DialogHelper;

public class KönyvSzerkesztoTest extends AbstractUiTest
{
    private KönyvszerkesztőTestHelper könyvSzerkesztő;
    private KönyvController könyvController;

    @Before
    public void setUp()
    {
        List<String> oszlopok = newArrayList("szimpla", "ac", "mmac");
        Könyv könyv = new Könyv.Builder(oszlopok.size())
                .withÉrték(0, "Érték1")
                .build();
        könyvController = new KönyvController();
        KönyvControllerInput könyvControllerInput = new KönyvControllerInput.Builder()
                .withKönyv(könyv)
                .withOszlopok(oszlopok)
                .withOszlopKonfiguráció(new OszlopKonfiguráció.Builder(3, 4)
                        .withRow("", MULTIMEZŐ.getNév(), AUTOCOMPLETE.getNév())
                        .withRow(oszlopok.get(0), "", "")
                        .withRow(oszlopok.get(1), "", "igen")
                        .withRow(oszlopok.get(2), "igen", "igen")
                        .build())
                .withKönyvLista(newArrayList(könyv
                        , new Könyv.Builder(oszlopok.size())
                                .withÉrték(0, "Érték2")
                                .build()))
                .build();
        SwingUtilities.invokeLater(() -> DialogHelper.startModalDialog(null, könyvController, könyvControllerInput));
        könyvSzerkesztő = new KönyvszerkesztőTestHelper(BasicRobot.robotWithCurrentAwtHierarchy());
    }

    @After
    public void tearDown()
    {
        könyvSzerkesztő.cleanUp();
    }

    @Test
    public void testNormalField()
    {
        könyvSzerkesztő.setNormalText("szimpla", "szimpla");
        könyvSzerkesztő.clickMentés();
        assertThat(könyvController.getOutput().getValue(0)).isEqualTo("szimpla");
    }

    @Test
    public void testAutocomplete()
    {
        könyvSzerkesztő.setComboBoxText("ac", "auto complete");
        könyvSzerkesztő.clickMentés();
        assertThat(könyvController.getOutput().getValue(1)).isEqualTo("auto complete");
    }

    @Test
    public void test2()
    {
        könyvSzerkesztő.clickMentés();
    }
}
