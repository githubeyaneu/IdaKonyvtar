package eu.eyan.idakonyvtar;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eyan.idakonyvtar.controller.KönyvtárController;
import eu.eyan.idakonyvtar.model.OszlopKonfiguráció.OszlopKonfigurációk;
import eu.eyan.idakonyvtar.testhelper.ExcelAssert;
import eu.eyan.idakonyvtar.testhelper.IdaKönyvtárTestHelper;
import eu.eyan.idakonyvtar.testhelper.KönyvtárFileBuilder;
import eu.eyan.idakonyvtar.util.ExcelKezelő;
import eu.eyan.idakonyvtar.util.KiemelőRenderer;
import eu.eyan.idakonyvtar.view.KönyvtárMenüAndToolBar;

public class IdaKonyvtarTest extends AbstractUiTest
{
    private static IdaKönyvtárTestHelper könyvtár = new IdaKönyvtárTestHelper();

    @Before
    public void setUp()
    {
        könyvtár.start(null);
    }

    @After
    public void tearDown()
    {
        könyvtár.cleanUp();
    }

    @Test
    public void testStartProgram()
    {
        könyvtár.requireVisible();
        könyvtár.requireTitle(KönyvtárController.TITLE);
    }

    @Test
    public void testMenü()
    {
        könyvtár.clickMenü(KönyvtárMenüAndToolBar.ISBN_KERESÉS);
        könyvtár.szerkesztő().clickMégsem();
        könyvtár.clickMenü(KönyvtárMenüAndToolBar.FILE);
    }

    @Test
    public void testBetöltésÉsMentés()
    {
        File file = new KönyvtárFileBuilder()
                .withSheet(ExcelKezelő.KÖNYVEK)
                .withOszlopok("oszlop1", "oszlop2")
                .withSor("árvíztűrő tükörfúrógép", "ÁRVÍZTŰRŐ TÜKÖRFÚRÓGÉP")
                .withSheet(ExcelKezelő.OSZLOP_KONFIGURÁCIÓ)
                .withOszlopok("", OszlopKonfigurációk.MEGJELENÍTÉS_TÁBLÁZATBAN.getNév(), "ko2")
                .withSor("oszlop1", "igen", "")
                .withSor("oszlop2 tükörfúrógép", "nem", "")
                .save();
        try
        {
            könyvtár.betölt(file);
            könyvtár.assertTáblázatCella(1, 1, "árvíztűrő tükörfúrógép");
        }
        finally
        {
            file.delete();
        }
        File file2 = new File(System.currentTimeMillis() + ".xls");
        try
        {
            könyvtár.ment(file2);
            ExcelAssert.assertExcelCella(file2, ExcelKezelő.KÖNYVEK, 1, 2, "árvíztűrő tükörfúrógép");
        }
        finally
        {
            file2.delete();
        }
    }

    @Test
    public void testÚjKönyvMent()
    {
        könyvtár.assertTáblázatCella(2, 1, "A napok hordaléka");
        könyvtár.clickÚjGomb();
        könyvtár.szerkesztő().requireIsbnPresent();
        könyvtár.szerkesztő().setNormalText("Cím", "Cím1");
        könyvtár.szerkesztő().clickMentés();
        könyvtár.assertTáblázatCella(2, 1, "Cím1");
        könyvtár.assertTáblázatCella(2, 2, "A napok hordaléka");
    }

    @Test
    public void testÚjKönyvNemMent()
    {
        könyvtár.assertTáblázatCella(2, 1, "A napok hordaléka");
        könyvtár.clickÚjGomb();
        könyvtár.szerkesztő().setNormalText("Cím", "Cím1");
        könyvtár.szerkesztő().clickMégsem();
        könyvtár.assertTáblázatCella(2, 1, "A napok hordaléka");
    }

    @Test
    public void testKönyvTörlésOk()
    {
        könyvtár.assertTáblázatCella(2, 1, "A napok hordaléka");
        könyvtár.requireTörlésDisabled();
        könyvtár.sorKiválasztása(1);
        könyvtár.requireTörlésEnabled();
        könyvtár.clickTörlésGomb();
        könyvtár.clickMegerősítésIgen();
        könyvtár.assertTáblázatCella(2, 1, "Újabb napok hordaléka");
        könyvtár.requireTörlésDisabled();
    }

    @Test
    public void testKönyvTörlésCancel()
    {
        könyvtár.assertTáblázatCella(2, 1, "A napok hordaléka");
        könyvtár.requireTörlésDisabled();
        könyvtár.sorKiválasztása(1);
        könyvtár.requireTörlésEnabled();
        könyvtár.clickTörlésGomb();
        könyvtár.clickMegerősítésNem();
        könyvtár.assertTáblázatCella(2, 1, "A napok hordaléka");
        könyvtár.requireTörlésEnabled();
    }

    @Test
    public void testSzürés()
    {
        könyvtár.szűrés("aron");
        könyvtár.assertTáblázatCella(1, 1, new StringBuilder("")
                .append(KiemelőRenderer.HTML_START_TAG)
                .append("Tamási ")
                .append(KiemelőRenderer.KIEMELÉS_START_TAG)
                .append("Áron")
                .append(KiemelőRenderer.KIEMELÉS_END_TAG)
                .append(KiemelőRenderer.HTML_END_TAG)
                .toString());
        könyvtár.assertTáblázatCella(2, 2, new StringBuilder("")
                .append(KiemelőRenderer.HTML_START_TAG)
                .append("Kh")
                .append(KiemelőRenderer.KIEMELÉS_START_TAG)
                .append("áron")
                .append(KiemelőRenderer.KIEMELÉS_END_TAG)
                .append(" ladikján vagy az öregedés tünetei : esszé-regény")
                .append(KiemelőRenderer.HTML_END_TAG)
                .toString());
        könyvtár.assertTáblázatCella(1, 2, "Illyés Gyula");
        könyvtár.assertTáblázatSorok(2);
    }

    @Test
    public void testKönyvSzerkesztMent()
    {
        könyvtár.assertTáblázatCella(2, 1, "A napok hordaléka");
        könyvtár.duplaKlick(1);
        könyvtár.szerkesztő().requireIsbnNotPresent();
        könyvtár.szerkesztő().setNormalText("Cím", "Cím1");
        könyvtár.szerkesztő().clickMentés();
        könyvtár.assertTáblázatCella(2, 1, "Cím1");
    }

    @Test
    public void testKönyvSzerkesztMégsem()
    {
        könyvtár.assertTáblázatCella(2, 1, "A napok hordaléka");
        könyvtár.duplaKlick(1);
        könyvtár.szerkesztő().setNormalText("Cím", "Cím1");
        könyvtár.szerkesztő().clickMégsem();
        könyvtár.assertTáblázatCella(2, 1, "A napok hordaléka");
    }
}
