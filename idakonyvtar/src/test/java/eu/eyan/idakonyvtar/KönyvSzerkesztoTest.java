package eu.eyan.idakonyvtar;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eyan.idakonyvtar.model.OszlopKonfiguráció.OszlopKonfigurációk.AUTOCOMPLETE;
import static eu.eyan.idakonyvtar.model.OszlopKonfiguráció.OszlopKonfigurációk.MULTIMEZŐ;
import static org.fest.assertions.Assertions.assertThat;

import java.awt.event.KeyEvent;
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
        List<String> oszlopok = newArrayList("szimpla", "ac", "mm", "mmac");
        Könyv könyv = new Könyv.Builder(oszlopok.size())
                .withÉrték(0, "Érték1")
                .build();
        könyvController = new KönyvController();
        KönyvControllerInput könyvControllerInput = new KönyvControllerInput.Builder()
                .withKönyv(könyv)
                .withOszlopok(oszlopok)
                .withOszlopKonfiguráció(new OszlopKonfiguráció.Builder(3, oszlopok.size() + 1)
                        .withRow("", MULTIMEZŐ.getNév(), AUTOCOMPLETE.getNév())
                        .withRow(oszlopok.get(0), "", "")
                        .withRow(oszlopok.get(1), "", "igen")
                        .withRow(oszlopok.get(2), "igen", "")
                        .withRow(oszlopok.get(3), "igen", "igen")
                        .build())
                .withKönyvLista(newArrayList(
                        könyv
                        , new Könyv.Builder(oszlopok.size())
                                .withÉrték(0, "Érték2")
                                .withÉrték(1, "abc")
                                .withÉrték(3, "abc")
                                .build(),
                        new Könyv.Builder(oszlopok.size())
                                .withÉrték(0, "Érték2")
                                .withÉrték(1, "abd")
                                .withÉrték(3, "abd")
                                .build()))
                .build();
        SwingUtilities.invokeLater(() -> DialogHelper.startModalDialog(null, könyvController, könyvControllerInput));
        könyvSzerkesztő = new KönyvszerkesztőTestHelper(BasicRobot.robotWithCurrentAwtHierarchy());
    }

    public static void main(String[] args)
    {
        new KönyvSzerkesztoTest().setUp();
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
    public void testAutocompleteDefault()
    {
        könyvSzerkesztő.setComboBoxText("ac", "a");
        könyvSzerkesztő.keyboard(KeyEvent.VK_ESCAPE);
        könyvSzerkesztő.clickMentés();
        assertThat(könyvController.getOutput().getValue(1)).isEqualTo("abc");
    }

    @Test
    public void testAutocompleteNew()
    {
        könyvSzerkesztő.setComboBoxText("ac", "a");
        könyvSzerkesztő.keyboard(KeyEvent.VK_DELETE);
        könyvSzerkesztő.keyboard(KeyEvent.VK_ESCAPE);
        könyvSzerkesztő.clickMentés();
        assertThat(könyvController.getOutput().getValue(1)).isEqualTo("a");
    }

    @Test
    public void testMultiMezo()
    {
        könyvSzerkesztő.setNormalText("mm1", "");
        könyvSzerkesztő.enterNormalText("mm1", "a");
        könyvSzerkesztő.enterNormalText("mm2", "b");
        könyvSzerkesztő.enterNormalText("mm1", "b");
        könyvSzerkesztő.enterNormalText("mm3", "c");
        könyvSzerkesztő.multimezőTöröl("mm", 2);
        könyvSzerkesztő.requireTörölDisabled("mm", 4);
        könyvSzerkesztő.clickMentés();
        assertThat(könyvController.getOutput().getValue(2)).isEqualTo("ab + c");
    }

    @Test
    public void testMultiMezoAutoComplete()
    {
        könyvSzerkesztő.setComboBoxText("mmac1", "");
        könyvSzerkesztő.enterComboBoxText("mmac1", "a");
        könyvSzerkesztő.enterComboBoxText("mmac2", "b");
        könyvSzerkesztő.enterComboBoxText("mmac1", "b");
        könyvSzerkesztő.enterComboBoxText("mmac3", "c");
        könyvSzerkesztő.multimezőTöröl("mmac", 2);
        könyvSzerkesztő.requireTörölDisabled("mmac", 4);
        könyvSzerkesztő.enterComboBoxText("mmac4", "a");
        könyvSzerkesztő.clickMentés();
        assertThat(könyvController.getOutput().getValue(3)).isEqualTo("b + c + abc");
    }

    @Test
    public void test2()
    {
        könyvSzerkesztő.clickMentés();
    }
}
