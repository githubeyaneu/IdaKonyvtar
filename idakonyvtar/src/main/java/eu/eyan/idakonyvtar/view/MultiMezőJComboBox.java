package eu.eyan.idakonyvtar.view;

import static com.google.common.collect.Lists.newArrayList;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

public class MultiMezőJComboBox extends MultiMező<String, JComboBox<String>>
{
    private static final long serialVersionUID = 1L;
    private List<String> oszlopLista = newArrayList();

    @Override
    protected void addMezőEditListener(JComboBox<String> editor, MezőEditListener<JComboBox<String>> listener)
    {
        ((JTextField) editor.getEditor().getEditorComponent()).addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                listener.mezőEdited(editor);
            }
        });
    }

    @Override
    protected JComboBox<String> getEditor()
    {
        JComboBox<String> jComboBox = new JComboBox<String>();
        AutoCompleteDecorator.decorate(jComboBox);
        jComboBox.setModel(new ListComboBoxModel<String>(oszlopLista));
        jComboBox.setEditable(true);
        return jComboBox;
    }

    @Override
    protected String getValue(JComboBox<String> editor)
    {
        String text = ((JTextField) editor.getEditor().getEditorComponent()).getText().trim();
        return text.equals("") ? null : text;
    }

    @Override
    protected void setValueInEditor(JComboBox<String> editor, String value)
    {
        ((JTextField) editor.getEditor().getEditorComponent()).setText(value);
    }

    public void setAutoCompleteLista(List<String> oszlopLista)
    {
        this.oszlopLista = oszlopLista;
    }

}
