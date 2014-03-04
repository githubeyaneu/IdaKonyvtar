package eu.eyan.idakonyvtar;

import static org.fest.assertions.Assertions.assertThat;

import java.awt.Point;
import java.io.File;

import org.fest.swing.core.MouseButton;
import org.fest.swing.core.matcher.JButtonMatcher;
import org.fest.swing.fixture.FrameFixture;

import eu.eyan.idakonyvtar.controller.KönyvtárController;
import eu.eyan.idakonyvtar.view.KönyvtárMenüAndToolBar;

public class IdaKönyvtárTestHelper
{
    private FrameFixture frame;

    public void start(String filenév)
    {
        IdaKonyvtar.main(new String[] { filenév });
        frame = new FrameFixture(KönyvtárController.TITLE);
    }

    public void requireVisible()
    {
        assertThat(frame.target.isVisible()).isTrue();
    }

    public void requireTitle(String title)
    {
        assertThat(frame.target.getTitle()).isEqualTo(title);
    }

    public void close()
    {
        frame.target.setVisible(false);
    }

    public void clickMenü(String menüpont)
    {
        frame.menuItem(menüpont).click();
    }

    public void betölt(File file)
    {
        clickMenü(KönyvtárMenüAndToolBar.FILE);
        clickMenü(KönyvtárMenüAndToolBar.KÖNYVTÁR_BETÖLTÉSE);
        frame.fileChooser().selectFile(file).approve();
    }

    public void assertTáblázatCella(int col, int row, String content)
    {
        assertThat(frame.table().contents()[row - 1][col - 1]).isEqualTo(content);
    }

    public void ment(File file)
    {
        clickMenü(KönyvtárMenüAndToolBar.FILE);
        clickMenü(KönyvtárMenüAndToolBar.KÖNYVTÁR_MENTÉSE);
        frame.fileChooser().selectFile(file).approve();
    }

    public void clickÚjGomb()
    {
        frame.button(KönyvtárMenüAndToolBar.ÚJ_KÖNYV_HOZZÁADÁSA).click();

    }

    public KönyvszerkesztőTestHelper szerkesztő()
    {
        return new KönyvszerkesztőTestHelper(frame.dialog());
    }

    public void requireTörlésDisabled()
    {
        frame.button(KönyvtárMenüAndToolBar.KÖNYV_TÖRLÉSE).requireDisabled();

    }

    public void sorKiválasztása(int sor)
    {
        frame.table().selectRows(sor - 1);
    }

    public void requireTörlésEnabled()
    {
        frame.button(KönyvtárMenüAndToolBar.KÖNYV_TÖRLÉSE).requireEnabled();
    }

    public void clickTörlésGomb()
    {
        frame.button(KönyvtárMenüAndToolBar.KÖNYV_TÖRLÉSE).click();
    }

    public void clickMegerősítésIgen()
    {
        frame.dialog().button(JButtonMatcher.withText(KönyvtárController.MEGERŐSÍTÉS_IGEN)).click();
    }

    public void clickMegerősítésNem()
    {
        frame.dialog().button(JButtonMatcher.withText(KönyvtárController.MEGERŐSÍTÉS_NEM)).click();
    }

    public void szűrés(String szűrő)
    {
        frame.textBox(KönyvtárMenüAndToolBar.SZŰRÉS).robot.enterText(szűrő);
    }

    public void assertTáblázatSorok(int sorokSzáma)
    {
        frame.table().requireRowCount(sorokSzáma);
    }

    public void duplaKlick(int sor)
    {
        int rowHeight = frame.table().target.getRowHeight();
        Point elsőSor = frame.table().target.getLocationOnScreen();
        elsőSor.translate(3, rowHeight * (sor - 1) + rowHeight / 2);
        frame.table().robot.moveMouse(elsőSor);
        frame.table().robot.click(elsőSor, MouseButton.LEFT_BUTTON, 2);
    }

    public void cleanUp()
    {
        frame.cleanUp();
    }
}
