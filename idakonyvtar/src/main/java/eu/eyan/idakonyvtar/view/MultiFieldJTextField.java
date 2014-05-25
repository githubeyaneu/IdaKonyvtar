package eu.eyan.idakonyvtar.view;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

public class MultiFieldJTextField extends MultiField<String, JTextField>
{
    public MultiFieldJTextField(String columnName)
    {
        super(columnName);
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
    protected void addFieldEditListener(JTextField editorComponent, FieldEditListener<JTextField> listener)
    {
        editorComponent.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                listener.fieldEdited(editorComponent);
            }
        });
    }
}
