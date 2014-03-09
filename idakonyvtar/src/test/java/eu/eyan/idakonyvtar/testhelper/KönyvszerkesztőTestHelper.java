package eu.eyan.idakonyvtar.testhelper;

import static org.fest.swing.finder.WindowFinder.findDialog;

import javax.swing.JDialog;

import org.fest.assertions.Fail;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.ContainerFixture;
import org.fest.swing.fixture.DialogFixture;

import eu.eyan.idakonyvtar.util.DialogHelper;
import eu.eyan.idakonyvtar.view.KönyvView;

public class KönyvszerkesztőTestHelper
{

    public static final GenericTypeMatcher<JDialog> VISIBLE_DIALOG_FINDER = new GenericTypeMatcher<JDialog>(JDialog.class)
    {
        @Override
        protected boolean isMatching(JDialog jDialog)
        {
            return jDialog.isVisible();
        }
    };

    private DialogFixture dialog;

    public KönyvszerkesztőTestHelper(Robot robot)
    {
        dialog = findDialog(VISIBLE_DIALOG_FINDER).withTimeout(1000).using(robot);
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

    public void setNormalText(String textBoxNév, String szöveg)
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

    public void cleanUp()
    {
        dialog.cleanUp();
    }

    public void setComboBoxText(String comboBoxName, String value)
    {
        dialog.comboBox(comboBoxName).enterText(value);
    }
}
