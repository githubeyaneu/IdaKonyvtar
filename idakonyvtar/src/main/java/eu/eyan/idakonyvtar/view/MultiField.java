package eu.eyan.idakonyvtar.view;

import static com.google.common.collect.Lists.newArrayList;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import lombok.Getter;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public abstract class MultiField<INPUT, EDITOR extends Component> extends JPanel implements FieldEditListener<EDITOR>
{
    private static final long serialVersionUID = 1L;
    private final List<Field<EDITOR>> fields = newArrayList();
    private String columnName;
    private int counter = 1;

    private static class Field<EDITOR>
    {
        @Getter
        private final EDITOR editor;
        @Getter
        private final JButton delete;
        @Getter
        private final JPanel panel;

        public Field(EDITOR editor, JButton button, JPanel mezőPanel)
        {
            this.editor = editor;
            this.delete = button;
            this.panel = mezőPanel;
        }
    }

    public MultiField(String columnName)
    {
        this.columnName = columnName;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void setValues(List<INPUT> values)
    {
        removeAll();
        fields.clear();

        for (INPUT input : values)
        {
            addEditor(input, false);
        }
        addEditor(null, true);
    }

    private void addEditor(INPUT input, boolean last)
    {
        EDITOR editor = getEditor();
        addFieldEditListener(editor, this);
        JButton deleteButton = new JButton("x");

        PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("f:p:g, 3dlu, 30dlu", "f:p:g, 3dlu"));
        panelBuilder.add(editor, CC.xy(1, 1));
        panelBuilder.add(deleteButton, CC.xy(3, 1));
        JPanel fieldPanel = panelBuilder.build();

        if (last)
        {
            deleteButton.setEnabled(false);
        }
        else
        {
            setValueInEditor(editor, input);
        }

        final Field<EDITOR> field = new Field<EDITOR>(editor, deleteButton, fieldPanel);
        deleteButton.addActionListener((ActionEvent actionEvent) -> {
            fields.remove(field);
            remove(field.getPanel());
            revalidate();
        });
        fields.add(field);
        add(fieldPanel);

        fieldPanel.setName(columnName + ".panel." + counter);
        editor.setName(columnName + counter);
        deleteButton.setName(columnName + ".delete." + counter);
        counter++;

        revalidate();
//        SwingUtilities.getWindowAncestor(this).pack();
    }

    protected abstract void addFieldEditListener(EDITOR editor, FieldEditListener<EDITOR> listener);

    @Override
    public void fieldEdited(EDITOR source)
    {
        Field<EDITOR> lastField = fields.get(fields.size() - 1);
        if (lastField.getEditor() == source)
        {
            lastField.getDelete().setEnabled(true);
            addEditor(null, true);
        }
    }

    protected abstract EDITOR getEditor();

    public List<INPUT> getValues()
    {
        return fields.stream().map((Field<EDITOR> field) -> {
            return getValue(field.getEditor());
        }).collect(Collectors.toList());
    }

    /**
     * @return null if empty!
     */
    protected abstract INPUT getValue(EDITOR editor);

    protected abstract void setValueInEditor(EDITOR editor, INPUT value);
}
