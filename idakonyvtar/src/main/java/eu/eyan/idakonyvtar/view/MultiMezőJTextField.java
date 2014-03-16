package eu.eyan.idakonyvtar.view;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

public class MultiMezőJTextField extends MultiMező<String, JTextField>
{
    public MultiMezőJTextField(String oszlopNév)
    {
        super(oszlopNév);
    }

    private static final long serialVersionUID = 1L;

    @Override
    protected JTextField getEditor()
    {
        return new JTextField();
    }

    @Override
    protected String getValue(JTextField editor)
    {
        String text = editor.getText().trim();
        return text.equals("") ? null : text;
    }

    @Override
    protected void setValueInEditor(JTextField editor, String value)
    {
        editor.setText(value);
    }

    @Override
    protected void addMezőEditListener(JTextField editorComponent, MezőEditListener<JTextField> listener)
    {
        editorComponent.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                listener.mezőEdited(editorComponent);
            }
        });
    }
}
