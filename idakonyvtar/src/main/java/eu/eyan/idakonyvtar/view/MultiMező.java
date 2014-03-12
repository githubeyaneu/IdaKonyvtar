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

public abstract class MultiMező<INPUT, EDITOR extends Component> extends JPanel implements MezőEditListener<EDITOR>
{
    private static final long serialVersionUID = 1L;
    private final List<Mező<EDITOR>> mezők = newArrayList();

    private static class Mező<EDITOR>
    {
        @Getter
        private final EDITOR editor;
        @Getter
        private final JButton törlés;
        @Getter
        private final JPanel panel;

        public Mező(EDITOR editor, JButton button, JPanel mezőPanel)
        {
            this.editor = editor;
            this.törlés = button;
            this.panel = mezőPanel;
        }
    }

    public MultiMező()
    {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void setValues(List<INPUT> values)
    {
        removeAll();
        mezők.clear();

        for (INPUT input : values)
        {
            addEditor(input, false);
        }
        addEditor(null, true);
    }

    private void addEditor(INPUT input, boolean utolsó)
    {
        EDITOR editor = getEditor();
        addMezőEditListener(editor, this);
        JButton button = new JButton("x");

        PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("f:p:g, 3dlu, 30dlu", "f:p:g, 3dlu"));
        panelBuilder.add(editor, CC.xy(1, 1));
        panelBuilder.add(button, CC.xy(3, 1));
        JPanel mezőPanel = panelBuilder.build();

        if (utolsó)
        {
            button.setEnabled(false);
        }
        else
        {
            setValueInEditor(editor, input);
        }

        final Mező<EDITOR> mező = new Mező<EDITOR>(editor, button, mezőPanel);
        button.addActionListener((ActionEvent actionEvent) -> {
            mezők.remove(mező);
            remove(mező.getPanel());
            revalidate();
        });
        mezők.add(mező);
        add(mezőPanel);
        revalidate();
//        SwingUtilities.getWindowAncestor(this).pack();
    }

    protected abstract void addMezőEditListener(EDITOR editor, MezőEditListener<EDITOR> listener);

    @Override
    public void mezőEdited(EDITOR source)
    {
        Mező<EDITOR> utolsóMező = mezők.get(mezők.size() - 1);
        if (utolsóMező.getEditor() == source)
        {
            utolsóMező.getTörlés().setEnabled(true);
            addEditor(null, true);
        }
    }

    protected abstract EDITOR getEditor();

    public List<INPUT> getValues()
    {
        return mezők.stream().map((Mező<EDITOR> mező) -> {
            return getValue(mező.getEditor());
        }).collect(Collectors.toList());
    }

    /**
     * @return Null ha üres!
     */
    protected abstract INPUT getValue(EDITOR editor);

    protected abstract void setValueInEditor(EDITOR editor, INPUT value);
}
