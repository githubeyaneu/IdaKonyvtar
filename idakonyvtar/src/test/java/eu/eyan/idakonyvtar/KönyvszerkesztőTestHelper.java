package eu.eyan.idakonyvtar;

import org.fest.assertions.Fail;
import org.fest.swing.fixture.ContainerFixture;
import org.fest.swing.fixture.DialogFixture;

import eu.eyan.idakonyvtar.util.DialogHelper;
import eu.eyan.idakonyvtar.view.KönyvView;

public class KönyvszerkesztőTestHelper
{

    private DialogFixture dialog;

    public KönyvszerkesztőTestHelper(DialogFixture dialog)
    {
        this.dialog = dialog;
    }

    public void requireIsbnNotPresent()
    {
        requireLabelNotPresent(dialog, KönyvView.ISBN_LABEL);
        requireTextBoxNotPresent(dialog, KönyvView.ISBN_TEXT);
    }

    private void requireTextBoxNotPresent(ContainerFixture<?> container, String textBoxName)
    {
        try
        {
            container.label(textBoxName);
        }
        catch (Exception e)
        {
            return;
        }
        Fail.fail();
    }

    private void requireLabelNotPresent(ContainerFixture<?> container, String labelName)
    {
        try
        {
            container.label(labelName).requireVisible();
        }
        catch (Exception e)
        {
            return;
        }
        Fail.fail();
    }

    public void requireIsbnPresent()
    {
        dialog.label(KönyvView.ISBN_LABEL).requireVisible();
        dialog.textBox(KönyvView.ISBN_TEXT).requireVisible();
    }

    public void setText(String textBoxNév, String szöveg)
    {
        dialog.textBox(textBoxNév).setText(szöveg);
    }

    public void clickMentés()
    {
        dialog.button(DialogHelper.MENTÉS).click();
    }

    public void clickMégsem()
    {
        dialog.button(DialogHelper.MÉGSEM).click();
    }

}
